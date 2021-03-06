package mchorse.blockbuster.client.particles.emitter;

import mchorse.blockbuster.client.particles.BedrockScheme;
import mchorse.blockbuster.client.particles.components.IComponentEmitterInitialize;
import mchorse.blockbuster.client.particles.components.IComponentEmitterUpdate;
import mchorse.blockbuster.client.particles.components.IComponentParticleInitialize;
import mchorse.blockbuster.client.particles.components.IComponentParticleRender;
import mchorse.blockbuster.client.particles.components.IComponentParticleUpdate;
import mchorse.blockbuster.client.textures.GifTexture;
import mchorse.mclib.math.Variable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.lwjgl.opengl.GL11;

import javax.vecmath.Matrix3f;
import javax.vecmath.Vector3d;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class BedrockEmitter
{
	public BedrockScheme scheme;
	public List<BedrockParticle> particles = new ArrayList<BedrockParticle>();

	public EntityLivingBase target;
	public World world;
	public boolean lit;
	public boolean running = true;
	private BedrockParticle particle;

	/* Intermediate values */
	public Vector3d lastGlobal = new Vector3d();
	public Matrix3f rotation = new Matrix3f();

	/* Runtime properties */
	private int age;
	private int lifetime;
	private boolean wasStopped;
	public double spawnedParticles;
	public boolean playing = true;

	public float random1 = (float) Math.random();
	public float random2 = (float) Math.random();
	public float random3 = (float) Math.random();
	public float random4 = (float) Math.random();

	private BlockPos.MutableBlockPos blockPos = new BlockPos.MutableBlockPos();

	/* Cached variable references to avoid hash look ups */
	private Variable varAge;
	private Variable varLifetime;
	private Variable varRandom1;
	private Variable varRandom2;
	private Variable varRandom3;
	private Variable varRandom4;

	private Variable varEmitterAge;
	private Variable varEmitterLifetime;
	private Variable varEmitterRandom1;
	private Variable varEmitterRandom2;
	private Variable varEmitterRandom3;
	private Variable varEmitterRandom4;

	public boolean isFinished()
	{
		return !this.running && this.particles.isEmpty();
	}

	public double getAge()
	{
		return this.getAge(0);
	}

	public double getAge(float partialTicks)
	{
		return (this.age + partialTicks) / 20.0;
	}

	public void setTarget(EntityLivingBase target)
	{
		this.target = target;
		this.world = target == null ? null : target.worldObj;
	}

	public void setWorld(World world)
	{
		this.world = world;
	}

	public void setScheme(BedrockScheme scheme)
	{
		this.scheme = scheme;

		if (this.scheme == null)
		{
			return;
		}

		this.lit = true;
		this.stop();
		this.start();

		this.setupVariables();
		this.setEmitterVariables(0);

		for (IComponentEmitterInitialize component : this.scheme.emitterInitializes)
		{
			component.apply(this);
		}
	}

	/* Variable related code */

	private void setupVariables()
	{
		this.varAge = scheme.parser.variables.get("variable.particle_age");
		this.varLifetime = scheme.parser.variables.get("variable.particle_lifetime");
		this.varRandom1 = scheme.parser.variables.get("variable.particle_random_1");
		this.varRandom2 = scheme.parser.variables.get("variable.particle_random_2");
		this.varRandom3 = scheme.parser.variables.get("variable.particle_random_3");
		this.varRandom4 = scheme.parser.variables.get("variable.particle_random_4");

		this.varEmitterAge = scheme.parser.variables.get("variable.emitter_age");
		this.varEmitterLifetime = scheme.parser.variables.get("variable.emitter_lifetime");
		this.varEmitterRandom1 = scheme.parser.variables.get("variable.emitter_random_1");
		this.varEmitterRandom2 = scheme.parser.variables.get("variable.emitter_random_2");
		this.varEmitterRandom3 = scheme.parser.variables.get("variable.emitter_random_3");
		this.varEmitterRandom4 = scheme.parser.variables.get("variable.emitter_random_4");
	}

	public void setParticleVariables(BedrockParticle particle, float partialTicks)
	{
		if (this.varAge != null) this.varAge.set(particle.getAge(partialTicks));
		if (this.varLifetime != null) this.varLifetime.set(particle.lifetime / 20.0);
		if (this.varRandom1 != null) this.varRandom1.set(particle.random1);
		if (this.varRandom2 != null) this.varRandom2.set(particle.random2);
		if (this.varRandom3 != null) this.varRandom3.set(particle.random3);
		if (this.varRandom4 != null) this.varRandom4.set(particle.random4);

		this.scheme.updateCurves();
	}

	public void setEmitterVariables(float partialTicks)
	{
		if (this.varEmitterAge != null) this.varEmitterAge.set(this.getAge(partialTicks));
		if (this.varEmitterLifetime != null) this.varEmitterLifetime.set(this.lifetime / 20.0);
		if (this.varEmitterRandom1 != null) this.varEmitterRandom1.set(this.random1);
		if (this.varEmitterRandom2 != null) this.varEmitterRandom2.set(this.random2);
		if (this.varEmitterRandom3 != null) this.varEmitterRandom3.set(this.random3);
		if (this.varEmitterRandom4 != null) this.varEmitterRandom4.set(this.random4);

		this.scheme.updateCurves();
	}

	public void start()
	{
		if (this.playing)
		{
			return;
		}

		this.age = 0;
		this.spawnedParticles = 0;
		this.playing = true;
	}

	public void stop()
	{
		if (!this.playing)
		{
			return;
		}

		this.age = 0;
		this.spawnedParticles = 0;
		this.playing = false;
		this.wasStopped = true;
	}

	/**
	 * Update this current emitter
	 */
	public void update()
	{
		if (this.scheme == null)
		{
			return;
		}

		this.setEmitterVariables(0);

		for (IComponentEmitterUpdate component : this.scheme.emitterUpdates)
		{
			component.update(this);
		}

		this.setEmitterVariables(0);
		this.updateParticles();

		if (!this.wasStopped)
		{
			this.age++;
		}

		this.wasStopped = false;
	}

	/**
	 * Update all particles
	 */
	private void updateParticles()
	{
		Iterator<BedrockParticle> it = this.particles.iterator();

		while (it.hasNext())
		{
			BedrockParticle particle = it.next();

			this.updateParticle(particle);

			if (particle.dead)
			{
				it.remove();
			}
		}
	}

	/**
	 * Update a single particle
	 */
	private void updateParticle(BedrockParticle particle)
	{
		particle.update(this);

		this.setParticleVariables(particle, 0);

		for (IComponentParticleUpdate component : this.scheme.particleUpdates)
		{
			component.update(this, particle);
		}
	}

	/**
	 * Spawn a particle
	 */
	public void spawnParticle()
	{
		if (!this.running)
		{
			return;
		}

		this.particles.add(this.createParticle(false));
	}

	/**
	 * Create a new particle
	 */
	private BedrockParticle createParticle(boolean forceRelative)
	{
		BedrockParticle particle = new BedrockParticle();

		this.setParticleVariables(particle, 0);
		particle.matrix.set(this.rotation);

		for (IComponentParticleInitialize component : this.scheme.particleInitializes)
		{
			component.apply(this, particle);
		}

		if (!particle.relativePosition && !forceRelative)
		{
			particle.position.add(this.lastGlobal);
			particle.initialPosition.add(this.lastGlobal);
		}

		particle.prevPosition.set(particle.position);
		particle.prevRotation = particle.rotation;

		return particle;
	}

	/**
	 * Render the particle on screen
	 */
	public void renderOnScreen(int x, int y, float scale)
	{
		if (this.scheme == null)
		{
			return;
		}

		float partialTicks = Minecraft.getMinecraft().getRenderPartialTicks();
		List<IComponentParticleRender> list = this.scheme.getComponents(IComponentParticleRender.class);

		if (!list.isEmpty())
		{
			GifTexture.bindTexture(this.scheme.texture);

			GlStateManager.enableBlend();
			GlStateManager.disableCull();

			if (this.particle == null || this.particle.dead)
			{
				this.particle = this.createParticle(true);
			}

			this.rotation.setIdentity();
			this.particle.update(this);
			this.setEmitterVariables(partialTicks);
			this.setParticleVariables(this.particle, partialTicks);

			for (IComponentParticleRender render : list)
			{
				render.renderOnScreen(this.particle, x, y, scale, partialTicks);
			}

			GlStateManager.disableBlend();
			GlStateManager.enableCull();
		}
	}

	/**
	 * Render all the particles in this particle emitter
	 */
	public void render(float partialTicks)
	{
		if (this.scheme == null)
		{
			return;
		}

		VertexBuffer builder = Tessellator.getInstance().getBuffer();
		List<IComponentParticleRender> renders = this.scheme.particleRender;

		for (IComponentParticleRender component : renders)
		{
			component.preRender(this, partialTicks);
		}

		if (!this.particles.isEmpty())
		{
			GifTexture.bindTexture(this.scheme.texture);
			builder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_LMAP_COLOR);

			for (BedrockParticle particle : this.particles)
			{
				this.setEmitterVariables(partialTicks);
				this.setParticleVariables(particle, partialTicks);

				for (IComponentParticleRender component : renders)
				{
					component.render(this, particle, builder, partialTicks);
				}
			}

			Tessellator.getInstance().draw();
		}

		for (IComponentParticleRender component : renders)
		{
			component.postRender(this, partialTicks);
		}
	}

	/**
	 * Get brightness for the block
	 */
	public int getBrightnessForRender(float partialTicks, double x, double y, double z)
	{
		if (this.lit)
		{
			return 15728880;
		}

		this.blockPos.setPos(x, y, z);

		return this.world.isBlockLoaded(this.blockPos) ? this.world.getCombinedLight(this.blockPos, 0) : 0;
	}
}