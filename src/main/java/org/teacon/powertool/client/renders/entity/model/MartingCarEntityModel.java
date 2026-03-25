package org.teacon.powertool.client.renders.entity.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import org.jspecify.annotations.NonNull;
import org.teacon.powertool.PowerTool;
import org.teacon.powertool.entity.MartingCarEntity;

public class MartingCarEntityModel<T extends MartingCarEntity> extends EntityModel<T> {
    public static final ModelLayerLocation LAYER_RED = new ModelLayerLocation(Identifier.fromNamespaceAndPath(PowerTool.MODID, "marting_car_red"), "main");
    public static final ModelLayerLocation LAYER_BLUE = new ModelLayerLocation(Identifier.fromNamespaceAndPath(PowerTool.MODID, "marting_car_green"), "main");
    public static final ModelLayerLocation LAYER_GREEN = new ModelLayerLocation(Identifier.fromNamespaceAndPath(PowerTool.MODID, "marting_car_blue"), "main");

    private final ModelPart kart;
    private final ModelPart seat;
    private final ModelPart steering;
    private final ModelPart inner;
    private final ModelPart main_frame;
    private final ModelPart right_arm;
    private final ModelPart left_arm;
    private final ModelPart front_wheels;
    private final ModelPart wheel;
    private final ModelPart wheel2;
    private final ModelPart rear_wheels;
    private final ModelPart wheel3;
    private final ModelPart wheel4;
    private final ModelPart axis;
    private final ModelPart player;

    public MartingCarEntityModel(ModelPart root) {
        this.kart = root.getChild("kart");
        this.seat = this.kart.getChild("seat");
        this.steering = this.kart.getChild("steering");
        this.inner = this.steering.getChild("inner");
        this.main_frame = this.kart.getChild("main_frame");
        this.right_arm = this.kart.getChild("right_arm");
        this.left_arm = this.kart.getChild("left_arm");
        this.front_wheels = this.kart.getChild("front_wheels");
        this.wheel = this.front_wheels.getChild("wheel");
        this.wheel2 = this.front_wheels.getChild("wheel2");
        this.rear_wheels = this.kart.getChild("rear_wheels");
        this.wheel3 = this.rear_wheels.getChild("wheel3");
        this.wheel4 = this.rear_wheels.getChild("wheel4");
        this.axis = this.kart.getChild("axis");
        this.player = root.getChild("player");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition kart = partdefinition.addOrReplaceChild("kart", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));

        PartDefinition seat = kart.addOrReplaceChild("seat", CubeListBuilder.create().texOffs(0, 33).addBox(-5.0F, -5.0F, -2.0F, 10.0F, 2.0F, 9.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 1.0F));

        PartDefinition cube_r1 = seat.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(0, 0).addBox(-5.0F, -11.0F, -2.0F, 10.0F, 11.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -3.0F, 7.0F, -0.2618F, 0.0F, 0.0F));

        PartDefinition steering = kart.addOrReplaceChild("steering", CubeListBuilder.create().texOffs(0, 22).addBox(-2.0F, -1.0F, -1.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -11.9497F, -6.2218F, -0.7854F, 0.0F, 0.0F));

        PartDefinition inner = steering.addOrReplaceChild("inner", CubeListBuilder.create().texOffs(37, 7).addBox(-6.0F, 0.0F, -3.0F, 12.0F, 0.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition main_frame = kart.addOrReplaceChild("main_frame", CubeListBuilder.create().texOffs(0, 0).addBox(-3.5F, -0.2929F, -6.0F, 8.0F, 2.0F, 27.0F, new CubeDeformation(0.0F))
                .texOffs(29, 33).addBox(-7.5F, -0.2929F, 8.0F, 16.0F, 1.0F, 5.0F, new CubeDeformation(0.0F))
                .texOffs(43, 0).addBox(-6.5F, -1.2929F, -9.0F, 14.0F, 3.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(38, 39).addBox(-7.5F, -1.2929F, 21.0F, 16.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(-0.5F, -2.7071F, -8.0F));

        PartDefinition cube_r2 = main_frame.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(0, 44).addBox(-3.0F, -8.0F, 0.0F, 6.0F, 12.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.5F, -4.2929F, -6.0F, -0.7854F, 0.0F, 0.0F));

        PartDefinition cube_r3 = main_frame.addOrReplaceChild("cube_r3", CubeListBuilder.create().texOffs(0, 13).addBox(-4.0F, -3.0F, -1.0F, 8.0F, 6.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.5F, -1.2929F, 18.0F, 0.7854F, 0.0F, 0.0F));

        PartDefinition cube_r4 = main_frame.addOrReplaceChild("cube_r4", CubeListBuilder.create().texOffs(31, 59).addBox(0.0F, -1.0F, -3.0F, 4.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(8.5F, -0.0429F, 24.0F, 0.0F, 0.3927F, 0.0F));

        PartDefinition cube_r5 = main_frame.addOrReplaceChild("cube_r5", CubeListBuilder.create().texOffs(17, 58).addBox(-4.0F, -1.0F, -3.0F, 4.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-7.5F, -0.0429F, 24.0F, 0.0F, -0.3927F, 0.0F));

        PartDefinition cube_r6 = main_frame.addOrReplaceChild("cube_r6", CubeListBuilder.create().texOffs(38, 52).addBox(0.0F, -1.0F, 0.0F, 5.0F, 3.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(7.5F, -0.0429F, -9.0F, 0.0F, -0.3927F, 0.0F));

        PartDefinition cube_r7 = main_frame.addOrReplaceChild("cube_r7", CubeListBuilder.create().texOffs(52, 48).addBox(-5.0F, -1.0F, 0.0F, 5.0F, 3.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-6.5F, -0.0429F, -9.0F, 0.0F, 0.3927F, 0.0F));

        PartDefinition right_arm = kart.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(52, 55).addBox(-1.0F, -5.9497F, 21.7071F, 4.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(43, 13).addBox(-1.0F, -2.9497F, 9.7071F, 4.0F, 2.0F, 10.0F, new CubeDeformation(0.0F)), PartPose.offset(-9.0F, -2.0503F, -13.7071F));

        PartDefinition cube_r8 = right_arm.addOrReplaceChild("cube_r8", CubeListBuilder.create().texOffs(20, 51).addBox(-1.0F, -3.0F, 0.0F, 4.0F, 2.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -0.2929F, 20.2929F, 0.7854F, 0.0F, 0.0F));

        PartDefinition left_arm = kart.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(52, 55).mirror().addBox(-3.0F, -5.9497F, 21.7071F, 4.0F, 2.0F, 4.0F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(43, 13).mirror().addBox(-3.0F, -2.9497F, 9.7071F, 4.0F, 2.0F, 10.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(9.0F, -2.0503F, -13.7071F));

        PartDefinition cube_r9 = left_arm.addOrReplaceChild("cube_r9", CubeListBuilder.create().texOffs(20, 51).mirror().addBox(-3.0F, -3.0F, 0.0F, 4.0F, 2.0F, 5.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, -0.2929F, 20.2929F, 0.7854F, 0.0F, 0.0F));

        PartDefinition front_wheels = kart.addOrReplaceChild("front_wheels", CubeListBuilder.create(), PartPose.offset(0.0F, -2.5F, -8.0F));

        PartDefinition wheel = front_wheels.addOrReplaceChild("wheel", CubeListBuilder.create().texOffs(0, 67).addBox(-2.0F, -1.0355F, -2.5F, 4.0F, 2.0711F, 5.0F, new CubeDeformation(0.0F))
                .texOffs(0, 60).addBox(-2.0F, -2.5F, -1.0355F, 4.0F, 5.0F, 2.0711F, new CubeDeformation(0.0F)), PartPose.offset(-9.0F, 0.0F, 0.0F));

        PartDefinition octagon_r1 = wheel.addOrReplaceChild("octagon_r1", CubeListBuilder.create().texOffs(0, 60).addBox(-2.0F, -2.5F, -1.0355F, 4.0F, 5.0F, 2.0711F, new CubeDeformation(0.0F))
                .texOffs(0, 67).addBox(-2.0F, -1.0355F, -2.5F, 4.0F, 2.0711F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.7854F, 0.0F, 0.0F));

        PartDefinition wheel2 = front_wheels.addOrReplaceChild("wheel2", CubeListBuilder.create().texOffs(0, 67).addBox(-2.0F, -1.0355F, -2.5F, 4.0F, 2.0711F, 5.0F, new CubeDeformation(0.0F))
                .texOffs(0, 60).addBox(-2.0F, -2.5F, -1.0355F, 4.0F, 5.0F, 2.0711F, new CubeDeformation(0.0F)), PartPose.offset(9.0F, 0.0F, 0.0F));

        PartDefinition octagon_r2 = wheel2.addOrReplaceChild("octagon_r2", CubeListBuilder.create().texOffs(0, 60).addBox(-2.0F, -2.5F, -1.0355F, 4.0F, 5.0F, 2.0711F, new CubeDeformation(0.0F))
                .texOffs(0, 67).addBox(-2.0F, -1.0355F, -2.5F, 4.0F, 2.0711F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.7854F, 0.0F, 0.0F));

        PartDefinition rear_wheels = kart.addOrReplaceChild("rear_wheels", CubeListBuilder.create(), PartPose.offset(0.0F, -2.5F, 9.0F));

        PartDefinition wheel3 = rear_wheels.addOrReplaceChild("wheel3", CubeListBuilder.create().texOffs(0, 67).addBox(-2.0F, -1.0355F, -2.5F, 4.0F, 2.0711F, 5.0F, new CubeDeformation(0.0F))
                .texOffs(0, 60).addBox(-2.0F, -2.5F, -1.0355F, 4.0F, 5.0F, 2.0711F, new CubeDeformation(0.0F)), PartPose.offset(9.0F, 0.0F, 0.0F));

        PartDefinition octagon_r3 = wheel3.addOrReplaceChild("octagon_r3", CubeListBuilder.create().texOffs(0, 60).addBox(-2.0F, -2.5F, -1.0355F, 4.0F, 5.0F, 2.0711F, new CubeDeformation(0.0F))
                .texOffs(0, 67).addBox(-2.0F, -1.0355F, -2.5F, 4.0F, 2.0711F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.7854F, 0.0F, 0.0F));

        PartDefinition wheel4 = rear_wheels.addOrReplaceChild("wheel4", CubeListBuilder.create().texOffs(0, 67).addBox(-2.0F, -1.0355F, -2.5F, 4.0F, 2.0711F, 5.0F, new CubeDeformation(0.0F))
                .texOffs(0, 60).addBox(-2.0F, -2.5F, -1.0355F, 4.0F, 5.0F, 2.0711F, new CubeDeformation(0.0F)), PartPose.offset(-9.0F, 0.0F, 0.0F));

        PartDefinition octagon_r4 = wheel4.addOrReplaceChild("octagon_r4", CubeListBuilder.create().texOffs(0, 60).addBox(-2.0F, -2.5F, -1.0355F, 4.0F, 5.0F, 2.0711F, new CubeDeformation(0.0F))
                .texOffs(0, 67).addBox(-2.0F, -1.0355F, -2.5F, 4.0F, 2.0711F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.7854F, 0.0F, 0.0F));

        PartDefinition axis = kart.addOrReplaceChild("axis", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition cube_r10 = axis.addOrReplaceChild("cube_r10", CubeListBuilder.create().texOffs(0, 29).addBox(-11.5F, -1.0F, -1.0F, 23.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -2.5F, 9.0F, -0.7854F, 0.0F, 0.0F));

        PartDefinition cube_r11 = axis.addOrReplaceChild("cube_r11", CubeListBuilder.create().texOffs(0, 29).addBox(-11.5F, -1.0F, -1.0F, 23.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -2.5F, -8.0F, -0.7854F, 0.0F, 0.0F));

        PartDefinition player = partdefinition.addOrReplaceChild("player", CubeListBuilder.create().texOffs(63, 63).addBox(-4.0F, -20.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F))
                .texOffs(95, 63).addBox(-4.0F, -20.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.5F))
                .texOffs(79, 79).addBox(-4.0F, -12.0F, -2.0F, 8.0F, 12.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(79, 95).addBox(-4.0F, -12.0F, -2.0F, 8.0F, 12.0F, 4.0F, new CubeDeformation(0.25F)), PartPose.offset(0.0F, 17.0F, 6.0F));

        PartDefinition left_pants_r1 = player.addOrReplaceChild("left_pants_r1", CubeListBuilder.create().texOffs(63, 111).addBox(0.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.25F))
                .texOffs(79, 111).addBox(0.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -1.5708F, -0.3927F, 0.0F));

        PartDefinition right_pants_r1 = player.addOrReplaceChild("right_pants_r1", CubeListBuilder.create().texOffs(63, 95).addBox(-4.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.25F))
                .texOffs(63, 79).addBox(-4.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -1.5708F, 0.3927F, 0.0F));

        PartDefinition right_sleeve_r1 = player.addOrReplaceChild("right_sleeve_r1", CubeListBuilder.create().texOffs(103, 95).addBox(-8.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.25F))
                .texOffs(103, 79).addBox(-8.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(111, 111).addBox(4.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.25F))
                .texOffs(95, 111).addBox(4.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -10.0F, 0.0F, -1.1781F, 0.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 128, 128);
    }

    @Override
    public void setupAnim(@NonNull MartingCarEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
    }

    @Override
    public void renderToBuffer(@NonNull PoseStack poseStack, @NonNull VertexConsumer buffer, int packedLight, int packedOverlay, int color) {
        kart.render(poseStack, buffer, packedLight, packedOverlay, color);
    }

    private float prevSteeringRad = 0;
    private float prevWheelsRad = 0;

    public void updateAnimate(@NonNull MartingCarEntity entity, float partialTicks) {
        var steeringRad = entity.getSteeringRotateRadian();
        inner.yRot = Mth.rotLerp(partialTicks, prevSteeringRad, steeringRad);
        // Fixme: rotation vector to euler angles, needs some linear algebra staff.
//        wheel.yRot = Mth.rotLerp(partialTicks, prevSteeringRad, steeringRad);
//        wheel2.yRot = Mth.rotLerp(partialTicks, prevSteeringRad, steeringRad);

        var wheelRad = entity.getWheelRotateRadian();
        front_wheels.xRot = Mth.rotLerp(partialTicks, prevWheelsRad, wheelRad);
        rear_wheels.xRot = Mth.rotLerp(partialTicks, prevWheelsRad, wheelRad);

        prevSteeringRad = inner.yRot;
        prevWheelsRad = rear_wheels.xRot;
    }
}
