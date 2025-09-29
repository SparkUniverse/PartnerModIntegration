/*
 * Copyright © 2025 ModCore Inc. All rights reserved.
 *
 * This code is part of ModCore Inc.’s Essential Partner Mod Integration
 * repository and is protected under copyright. For the full license, see:
 * https://github.com/EssentialGG/EssentialPartnerMod/tree/main/LICENSE
 *
 * You may modify, fork, and use the Mod, but may not retain ownership of
 * accepted contributions, claim joint ownership, or use Essential’s trademarks.
 */

package gg.essential.partnermod.asm;

import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraftforge.fml.common.asm.transformers.deobf.FMLDeobfuscatingRemapper;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FrameNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

import java.util.ListIterator;
import java.util.Objects;

public class EssentialPartnerClassTransformer implements IClassTransformer {
    private static final String PKG;
    static {
        String pkg = EssentialPartnerClassTransformer.class.getName();
        pkg = pkg.substring(0, pkg.length() - ".asm.EssentialPartnerClassTransformer".length());
        PKG = pkg.replace('.', '/');
    }
    private static final String EssentialPartner = PKG + "/EssentialPartner";
    private static final String ModalManager = PKG + "/modal/ModalManager";
    private static final String DrawEvent = PKG + "/modal/ModalManager$DrawEvent";

    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        if (transformedName.equals("net.minecraft.client.Minecraft")) {
            ClassNode classNode = new ClassNode();
            ClassReader reader = new ClassReader(basicClass);
            reader.accept(classNode, 0);

            for (MethodNode method : classNode.methods) {
                String methodName = FMLDeobfuscatingRemapper.INSTANCE.mapMethodName(classNode.name, method.name, method.desc);
                if (methodName.equals("init") || methodName.equals("func_71384_a")) {
                    InsnList list = new InsnList();
                    list.add(new TypeInsnNode(Opcodes.NEW, EssentialPartner));
                    list.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, EssentialPartner, "<init>", "()V", false));
                    // Note: Must be after `beginMinecraftLoading` (because prior to that Forge's EventBus class will
                    //       complain) but before `displayGuiScreen` (because we want to react to that event)
                    method.instructions.insertBefore(findConstant(method.instructions, "Post startup"), list);
                }
            }

            ClassWriter writer = new ClassWriter(0);
            classNode.accept(writer);
            return writer.toByteArray();
        }
        if (transformedName.equals("net.minecraft.client.renderer.EntityRenderer")) {
            ClassNode classNode = new ClassNode();
            ClassReader reader = new ClassReader(basicClass);
            reader.accept(classNode, 0);

            for (MethodNode method : classNode.methods) {
                String methodName = FMLDeobfuscatingRemapper.INSTANCE.mapMethodName(classNode.name, method.name, method.desc);
                if (methodName.equals("updateCameraAndRender") || methodName.equals("func_181560_a")) {
                    ListIterator<AbstractInsnNode> iterator = method.instructions.iterator();
                    while (iterator.hasNext()) {
                        AbstractInsnNode abstractNode = iterator.next();
                        if (abstractNode instanceof MethodInsnNode) {
                            MethodInsnNode node = (MethodInsnNode) abstractNode;
                            // INVOKESTATIC net/minecraftforge/client/ForgeHooksClient.drawScreen (Lnet/minecraft/client/gui/GuiScreen;IIF)V
                            if (node.getOpcode() == Opcodes.INVOKESTATIC
                                && node.owner.equals("net/minecraftforge/client/ForgeHooksClient")
                                && node.name.equals("drawScreen")
                            ) {
                                iterator.add(new MethodInsnNode(Opcodes.INVOKESTATIC, ModalManager, "drawScreenPriority", "()V", false));
                                break;
                            }
                        }
                    }
                }
            }

            ClassWriter writer = new ClassWriter(0);
            classNode.accept(writer);
            return writer.toByteArray();
        }
        if (transformedName.equals("net.minecraftforge.client.ForgeHooksClient")) {
            ClassNode classNode = new ClassNode();
            ClassReader reader = new ClassReader(basicClass);
            reader.accept(classNode, 0);

            for (MethodNode method : classNode.methods) {
                if (method.name.equals("drawScreen")) {
                    InsnList list = new InsnList();
                    list.add(new TypeInsnNode(Opcodes.NEW, DrawEvent));
                    list.add(new InsnNode(Opcodes.DUP));
                    list.add(new VarInsnNode(Opcodes.ILOAD, 1));
                    list.add(new VarInsnNode(Opcodes.ILOAD, 2));
                    list.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, DrawEvent, "<init>", "(II)V", false));
                    list.add(new VarInsnNode(Opcodes.ASTORE, 4));
                    list.add(new VarInsnNode(Opcodes.ALOAD, 4));
                    list.add(new MethodInsnNode(Opcodes.INVOKESTATIC, ModalManager, "preDraw", "(L" + DrawEvent + ";)V", false));
                    list.add(new VarInsnNode(Opcodes.ALOAD, 4));
                    list.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, DrawEvent, "mouseXChanged", "()Z", false));
                    LabelNode skipXWrite = new LabelNode();
                    list.add(new JumpInsnNode(Opcodes.IFEQ, skipXWrite));
                    list.add(new VarInsnNode(Opcodes.ALOAD, 4));
                    list.add(new FieldInsnNode(Opcodes.GETFIELD, DrawEvent, "mouseX", "I"));
                    list.add(new VarInsnNode(Opcodes.ISTORE, 1));
                    list.add(skipXWrite);
                    list.add(new FrameNode(Opcodes.F_APPEND, 1, new Object[]{DrawEvent}, 0, null));
                    list.add(new VarInsnNode(Opcodes.ALOAD, 4));
                    list.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, DrawEvent, "mouseYChanged", "()Z", false));
                    LabelNode skipYWrite = new LabelNode();
                    list.add(new JumpInsnNode(Opcodes.IFEQ, skipYWrite));
                    list.add(new VarInsnNode(Opcodes.ALOAD, 4));
                    list.add(new FieldInsnNode(Opcodes.GETFIELD, DrawEvent, "mouseY", "I"));
                    list.add(new VarInsnNode(Opcodes.ISTORE, 2));
                    list.add(skipYWrite);
                    list.add(new FrameNode(Opcodes.F_CHOP, 1, null, 0, null));
                    method.instructions.insertBefore(method.instructions.getFirst(), list);
                    // Didn't compute actual need, 16 is just an arbitrary value deemed definitely big enough
                    method.maxLocals = Math.max(method.maxLocals, 16);
                    method.maxStack = Math.max(method.maxStack, 16);
                }
            }

            ClassWriter writer = new ClassWriter(0);
            classNode.accept(writer);
            return writer.toByteArray();
        }
        return basicClass;
    }

    private AbstractInsnNode findConstant(InsnList list, Object value) {
        ListIterator<AbstractInsnNode> iterator = list.iterator();
        while (iterator.hasNext()) {
            AbstractInsnNode node = iterator.next();
            if (node instanceof LdcInsnNode && Objects.equals(((LdcInsnNode) node).cst, value)) {
                return node;
            }
        }
        throw new RuntimeException("Failed to find LDC `" + value + "` instruction");
    }
}
