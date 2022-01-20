package me.lizardofoz.drgflares.client;

import me.lizardofoz.drgflares.entity.FlareEntity;
import me.lizardofoz.drgflares.util.FlareColor;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.*;
import net.minecraft.client.render.*;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3f;
import java.util.HashMap;
import java.util.Map;

@Environment(EnvType.CLIENT)
public class FlareEntityRenderer extends EntityRenderer<FlareEntity>
{
    private final Map<FlareColor, Identifier> TEXTURES = new HashMap<>();
    private final int MAX_LIGHT = LightmapTextureManager.pack(15, 15);

    private final ModelPart rodModel;
    private final ModelPart metalModel;

    public FlareEntityRenderer(EntityRendererFactory.Context context)
    {
        super(context);

        //There reason to have 2 models is because the rod itself remains glowing in dark
        rodModel = new ModelData().getRoot()
                .addChild("rod", ModelPartBuilder.create()
                        .uv(0, 12)
                        .cuboid(-1, -9, -1, 2, 13, 2), ModelTransform.NONE)
                .createPart(32, 32);

        ModelPartData metalModelBuilder = new ModelData().getRoot();
        metalModelBuilder.addChild("bottom", ModelPartBuilder.create()
                .uv(0, 6)
                .cuboid(-2, -8, -2, 4, 2, 4), ModelTransform.NONE);
        metalModelBuilder.addChild("top", ModelPartBuilder.create()
                .uv(0, 0)
                .cuboid(-2, 1, -2, 4, 2, 4), ModelTransform.NONE);
        metalModel = metalModelBuilder.createPart(32, 32);

        for (FlareColor color : FlareColor.colors)
            TEXTURES.put(color, new Identifier("drg_flares", "textures/entity/drg_flare_" + color.toString() + ".png"));
    }

    @Override
    public void render(FlareEntity entity, float yaw, float subTickTime, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light)
    {
        super.render(entity, yaw, subTickTime, matrices, vertexConsumers, light);
        Vec3d velocity = entity.getVelocity().multiply(10);

        entity.frame(subTickTime);

        matrices.push();
        matrices.translate(0, 0.1f, 0);
        //Here's a trick - we want each flare to end up with a different rotation when laying on the floor.
        //We could use random.setSeed(entId), but this works as good as that, but much faster
        matrices.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(entity.getId() * 119));
        matrices.multiply(Vec3f.POSITIVE_X.getDegreesQuaternion(entity.rotation));
        matrices.multiply(Vec3f.POSITIVE_X.getDegreesQuaternion(MathHelper.sin((float) (velocity.x + 90) / 15) * 360));
        matrices.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(MathHelper.cos((float) (velocity.y + velocity.x * 200) / 15) * 360));
        matrices.scale(0.6f, 0.6f, 0.6f);

        VertexConsumer vertexConsumer = vertexConsumers.getBuffer(RenderLayer.getEntityCutout(TEXTURES.get(entity.color)));
        rodModel.render(matrices, vertexConsumer, MAX_LIGHT, OverlayTexture.DEFAULT_UV, 1, 1, 1, 1);
        metalModel.render(matrices, vertexConsumer, light, OverlayTexture.DEFAULT_UV, 1, 1, 1, 1);
        matrices.pop();
    }

    @Override
    public Identifier getTexture(FlareEntity entity)
    {
        return TEXTURES.get(entity.color);
    }
}