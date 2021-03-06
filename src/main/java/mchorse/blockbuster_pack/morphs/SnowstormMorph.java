package mchorse.blockbuster_pack.morphs;

import mchorse.blockbuster.Blockbuster;
import mchorse.blockbuster.client.RenderingHandler;
import mchorse.blockbuster.client.particles.emitter.BedrockEmitter;
import mchorse.blockbuster.client.render.RenderCustomModel;
import mchorse.blockbuster.utils.MatrixUtils;
import mchorse.mclib.utils.Interpolations;
import mchorse.metamorph.api.morphs.AbstractMorph;
import mchorse.metamorph.capabilities.morphing.IMorphing;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.vecmath.Matrix3f;
import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

public class SnowstormMorph extends AbstractMorph
{
	public static final Matrix4f matrix = new Matrix4f();
	public static final Vector4f vector = new Vector4f();

	public String scheme = "";

	@SideOnly(Side.CLIENT)
	private BedrockEmitter emitter;

	@SideOnly(Side.CLIENT)
	public List<BedrockEmitter> lastEmitters;

	public boolean local;

	private boolean initialized;

	public static Vector4f calculateGlobal(Matrix4f matrix, EntityLivingBase entity, float x, float y, float z, float partial)
	{
		vector.set(x, y, z, 1);

		matrix.transform(vector);

		vector.add(new Vector4f(
			(float) Interpolations.lerp(entity.prevPosX, entity.posX, partial),
			(float) Interpolations.lerp(entity.prevPosY, entity.posY, partial),
			(float) Interpolations.lerp(entity.prevPosZ, entity.posZ, partial),
			(float) 0
		));

		return vector;
	}

	public SnowstormMorph()
	{
		super();
		this.name = "snowstorm";
	}

	@SideOnly(Side.CLIENT)
	public BedrockEmitter getEmitter()
	{
		if (this.emitter == null)
		{
			this.emitter = new BedrockEmitter();
		}

		return this.emitter;
	}


	@SideOnly(Side.CLIENT)
	public List<BedrockEmitter> getLastEmitters()
	{
		if (this.lastEmitters == null)
		{
			this.lastEmitters = new ArrayList<BedrockEmitter>();
		}

		return this.lastEmitters;
	}

	public void setScheme(String key, boolean isRemote)
	{
		this.scheme = key;
		this.initialized = false;

		if (isRemote)
		{
			this.setClientScheme(key);
		}
	}

	@SideOnly(Side.CLIENT)
	private void setClientScheme(String key)
	{
		this.getEmitter().setScheme(Blockbuster.proxy.particles.presets.get(key));
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void renderOnScreen(EntityPlayer entityPlayer, int x, int y, float scale, float alpha)
	{
		this.getEmitter().renderOnScreen(x, y, scale);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void render(EntityLivingBase entityLivingBase, double x, double y, double z, float yaw, float partialTicks)
	{
		BedrockEmitter emitter = this.getEmitter();

		if (MatrixUtils.matrix != null)
		{
			Matrix4f parent = new Matrix4f(MatrixUtils.matrix);
			Matrix4f matrix4f = MatrixUtils.readModelView(matrix);

			parent.invert();
			parent.mul(matrix4f);

			Vector4f zero = calculateGlobal(parent, entityLivingBase, 0, 0, 0, partialTicks);

			emitter.lastGlobal.x = zero.x;
			emitter.lastGlobal.y = zero.y;
			emitter.lastGlobal.z = zero.z;
			emitter.rotation.setIdentity();

			Vector3f ax = new Vector3f(parent.m00, parent.m01, parent.m02);
			Vector3f ay = new Vector3f(parent.m10, parent.m11, parent.m12);
			Vector3f az = new Vector3f(parent.m20, parent.m21, parent.m22);

			ax.normalize();
			ay.normalize();
			az.normalize();

			emitter.rotation.setRow(0, ax);
			emitter.rotation.setRow(1, ay);
			emitter.rotation.setRow(2, az);

			for (BedrockEmitter last : this.getLastEmitters())
			{
				last.lastGlobal.set(emitter.lastGlobal);
				last.rotation.set(emitter.rotation);
			}

			this.initialized = true;
		}
		else
		{
			emitter.lastGlobal.x = Interpolations.lerp(entityLivingBase.prevPosX, entityLivingBase.posX, partialTicks);
			emitter.lastGlobal.y = Interpolations.lerp(entityLivingBase.prevPosY, entityLivingBase.posY, partialTicks);
			emitter.lastGlobal.z = Interpolations.lerp(entityLivingBase.prevPosZ, entityLivingBase.posZ, partialTicks);
			emitter.rotation.setIdentity();

			for (BedrockEmitter last : this.getLastEmitters())
			{
				last.lastGlobal.set(emitter.lastGlobal);
				last.rotation.set(emitter.rotation);
			}

			this.initialized = true;
		}

		if (this.initialized)
		{
			this.setupEmitter(emitter, entityLivingBase);
			RenderingHandler.addEmitter(emitter);

			for (BedrockEmitter last : this.getLastEmitters())
			{
				this.setupEmitter(last, entityLivingBase);
				RenderingHandler.addEmitter(last);
			}
		}
	}

	@Override
	public void update(EntityLivingBase target, IMorphing cap)
	{
		super.update(target, cap);

		if (target.worldObj.isRemote && this.initialized)
		{
			this.updateEmitter(target);
		}
	}

	@SideOnly(Side.CLIENT)
	private void updateEmitter(EntityLivingBase target)
	{
		this.setupEmitter(this.getEmitter(), target);
		this.getEmitter().update();

		Iterator<BedrockEmitter> it = this.getLastEmitters().iterator();

		while (it.hasNext())
		{
			BedrockEmitter last = it.next();

			this.setupEmitter(last, target);
			last.update();

			if (last.isFinished())
			{
				it.remove();
			}
		}
	}

	private void setupEmitter(BedrockEmitter emitter, EntityLivingBase target)
	{
		emitter.setTarget(target);
	}

	@Override
	public AbstractMorph clone(boolean isRemote)
	{
		SnowstormMorph morph = new SnowstormMorph();

		morph.name = this.name;
		morph.settings = this.settings;
		morph.scheme = this.scheme;
		morph.local = this.local;
		morph.setScheme(morph.scheme, isRemote);

		return morph;
	}

	@Override
	public float getWidth(EntityLivingBase entityLivingBase)
	{
		return 0.6F;
	}

	@Override
	public float getHeight(EntityLivingBase entityLivingBase)
	{
		return 1.8F;
	}

	@Override
	public boolean equals(Object obj)
	{
		boolean result = super.equals(obj);

		if (obj instanceof SnowstormMorph)
		{
			SnowstormMorph morph = (SnowstormMorph) obj;

			result = result && Objects.equals(this.scheme, morph.scheme);
			result = result && this.local == morph.local;
		}

		return result;
	}

	@Override
	public boolean canMerge(AbstractMorph morph, boolean isRemote)
	{
		if (morph instanceof SnowstormMorph)
		{
			SnowstormMorph snow = (SnowstormMorph) morph;

			if (!this.scheme.equals(snow.scheme) && isRemote)
			{
				this.merge(snow);
			}

			return true;
		}

		return super.canMerge(morph, isRemote);
	}

	@SideOnly(Side.CLIENT)
	private void merge(SnowstormMorph snow)
	{
		this.getEmitter().running = false;
		this.getLastEmitters().add(this.getEmitter());

		this.emitter = new BedrockEmitter();
		this.setScheme(snow.scheme, true);
	}

	@Override
	public void reset()
	{
		super.reset();

		this.scheme = "";
		this.initialized = false;
	}

	@Override
	public void fromNBT(NBTTagCompound tag)
	{
		super.fromNBT(tag);

		if (tag.hasKey("Scheme"))
		{
			this.setScheme(tag.getString("Scheme"), FMLCommonHandler.instance().getSide() == Side.CLIENT);
		}

		if (tag.hasKey("Local"))
		{
			this.local = tag.getBoolean("Local");
		}
	}

	@Override
	public void toNBT(NBTTagCompound tag)
	{
		super.toNBT(tag);

		tag.setString("Scheme", this.scheme);
		tag.setBoolean("Local", this.local);
	}
}