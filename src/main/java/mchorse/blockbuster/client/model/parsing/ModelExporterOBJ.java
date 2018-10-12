package mchorse.blockbuster.client.model.parsing;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import javax.vecmath.Matrix3f;
import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;

import mchorse.blockbuster.Blockbuster;
import mchorse.blockbuster.api.Model;
import mchorse.blockbuster.api.ModelLimb;
import mchorse.blockbuster.api.ModelPose;
import mchorse.blockbuster.api.ModelTransform;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelBox;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.model.PositionTextureVertex;
import net.minecraft.client.model.TexturedQuad;
import net.minecraft.util.math.Vec3d;

/**
 * Model exporter OBJ class
 * 
 * <i>slaps roof of ModelExporterOBJ</i> this bad boy can convert so much 
 * OBJ models from Blockbuster JSON models. 
 * 
 * TODO: Connected quads
 */
public class ModelExporterOBJ
{
    private Model data;
    private ModelPose pose;

    public ModelExporterOBJ(Model data, ModelPose pose)
    {
        this.data = data;
        this.pose = pose;
    }

    /**
     * Export given model into OBJ string 
     */
    public String export(String modelName)
    {
        String obj = "# OBJ generated by Blockbuster (version " + Blockbuster.VERSION + ")\n\nmtllib " + modelName + ".mtl\nusemtl default\n";

        Map<ModelLimb, Mesh> meshes = new HashMap<>();
        this.generateMeshes(meshes);

        return obj + this.generateBody(meshes);
    }

    /**
     * Find texture quads field in {@link ModelBox} class.
     */
    private Field findField()
    {
        Field field = null;

        for (Field f : ModelBox.class.getDeclaredFields())
        {
            if (f.getType().equals(TexturedQuad[].class))
            {
                field = f;
                field.setAccessible(true);

                return field;
            }
        }

        return null;
    }

    /**
     * Prepare and generate meshes. This method is responsible for 
     * turning model's limbs into boxes and also preparing 
     * transformation matrices for actual generation of OBJ geometry.
     */
    private void generateMeshes(Map<ModelLimb, Mesh> meshes)
    {
        ModelBase base = new ModelBase()
        {};

        base.textureWidth = this.data.texture[0];
        base.textureHeight = this.data.texture[1];

        for (ModelLimb limb : this.data.limbs.values())
        {
            ModelTransform transform = this.pose.limbs.get(limb.name);

            if (transform == null)
            {
                transform = ModelTransform.DEFAULT;
            }

            Matrix4f mat = new Matrix4f();
            mat.setIdentity();
            Matrix3f rotScale = new Matrix3f();
            rotScale.setIdentity();
            mat.setTranslation(new Vector3f(transform.translate));
            mat.m23 = -mat.m23;
            mat.m13 = -mat.m13;

            Matrix3f x = new Matrix3f();
            rotScale.m00 = transform.scale[0];
            rotScale.m11 = transform.scale[1];
            rotScale.m22 = transform.scale[2];

            Matrix3f rot = new Matrix3f();
            rot.setIdentity();
            x.setIdentity();
            x.rotZ((float) Math.toRadians(-transform.rotate[2]));
            rot.mul(x);
            x.setIdentity();
            x.rotY((float) Math.toRadians(-transform.rotate[1]));
            rot.mul(x);
            x.setIdentity();
            x.rotX((float) Math.toRadians(transform.rotate[0]));
            rot.mul(x);

            rotScale.mul(rot);
            mat.setRotationScale(rotScale);

            int w = limb.size[0];
            int h = limb.size[1];
            int d = limb.size[2];

            float ox = 1 - limb.anchor[0];
            float oy = limb.anchor[1];
            float oz = limb.anchor[2];

            ModelBox box = new ModelBox(new ModelRenderer(base), limb.texture[0], limb.texture[1], -w * ox, -h * oy, -d * oz, w, h, d, 0F, limb.mirror);

            meshes.put(limb, new Mesh(box, mat, rot));
        }
    }

    /**
     * Generate body of the OBJ file based on given prepared meshes
     */
    private String generateBody(Map<ModelLimb, Mesh> meshes)
    {
        String output = "";
        Field field = this.findField();
        /* Count of vertices, normals and UVs indices */
        int v = 1;
        int u = 1;
        int n = 1;

        Matrix4f scale = new Matrix4f();
        scale.setIdentity();
        scale.setScale(1 / 16F);
        scale.m11 = -scale.m11;

        for (Map.Entry<ModelLimb, Mesh> entry : meshes.entrySet())
        {
            TexturedQuad[] quads = null;
            ModelLimb limb = entry.getKey();
            Mesh mesh = entry.getValue();

            try
            {
                quads = (TexturedQuad[]) field.get(mesh.box);
            }
            catch (Exception e)
            {}

            /* Technically shouldn't ever happen, but just in case */
            if (quads == null) continue;

            Matrix4f mat = new Matrix4f(mesh.mat);
            Matrix3f rot = new Matrix3f(mesh.rot);

            if (!limb.parent.isEmpty())
            {
                ModelLimb parent = this.data.limbs.get(limb.parent);

                while (parent != null)
                {
                    Mesh parentMesh = meshes.get(parent);
                    Matrix4f mat2 = new Matrix4f(parentMesh.mat);
                    Matrix3f rot2 = new Matrix3f(parentMesh.rot);

                    mat2.mul(mat);
                    rot2.mul(rot);

                    mat = mat2;
                    rot = rot2;

                    parent = this.data.limbs.get(parent.parent);
                }
            }

            /* Downscale geometry and flip vertically */
            Matrix4f m = new Matrix4f(scale);
            m.mul(mat);
            mat = m;

            /* Flip vertically normals */
            Matrix3f r = new Matrix3f();
            r.setIdentity();
            r.m11 = -1;
            r.mul(rot);
            rot = r;

            output += "o " + limb.name.replaceAll("\\s", "_") + "\n";

            String vertices = "";
            String normals = "";
            String uvs = "";
            String faces = "";

            for (TexturedQuad quad : quads)
            {
                /* Calculating normal as done in TexturedQuad */
                Vec3d v1 = quad.vertexPositions[1].vector3D.subtractReverse(quad.vertexPositions[0].vector3D);
                Vec3d v2 = quad.vertexPositions[1].vector3D.subtractReverse(quad.vertexPositions[2].vector3D);
                Vec3d v3 = v2.crossProduct(v1).normalize();
                Vector3f normal = new Vector3f((float) v3.xCoord, (float) v3.yCoord, (float) v3.zCoord);
                String face = "f ";

                rot.transform(normal);
                normal.normalize();
                normals += String.format("vn %.4f %.4f %.4f\n", normal.x, normal.y, normal.z);

                /* We're iterating backward, because it's important for 
                 * normal generation. */
                for (int i = quad.nVertices - 1; i >= 0; i--)
                {
                    PositionTextureVertex vx = quad.vertexPositions[i];
                    Vector4f vec = new Vector4f((float) vx.vector3D.xCoord, (float) vx.vector3D.yCoord, (float) vx.vector3D.zCoord, 1);

                    mat.transform(vec);
                    vertices += String.format("v %.4f %.4f %.4f\n", vec.x, vec.y, vec.z);
                    uvs += String.format("vt %f %f\n", vx.texturePositionX, 1 - vx.texturePositionY);
                    face += v + "/" + u + "/" + n + " ";

                    v++;
                    u++;
                }

                faces += face.trim() + "\n";
                n++;
            }

            output += vertices + normals + uvs + faces;
        }

        return output;
    }

    /**
     * Temporary mesh structure which is used internally within this 
     * class for generating an OBJ
     */
    public static class Mesh
    {
        public ModelBox box;
        public Matrix4f mat;
        public Matrix3f rot;

        public Mesh(ModelBox box, Matrix4f mat, Matrix3f rot)
        {
            this.box = box;
            this.mat = mat;
            this.rot = rot;
        }
    }
}