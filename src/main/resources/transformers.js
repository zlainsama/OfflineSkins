var ASMAPI = Java.type('net.minecraftforge.coremod.api.ASMAPI');
var OPCODES = Java.type('org.objectweb.asm.Opcodes');

// net/minecraft/client/entity/player/AbstractClientPlayerEntity/func_110303_q (getLocationCape)
function transformMethod001(node) {
    for (var i = 0; i < node.instructions.size(); i++) {
        var insn = node.instructions.get(i);
        if (OPCODES.ARETURN === insn.getOpcode()) {
            var tmp = ASMAPI.getMethodNode();
            tmp.visitVarInsn(OPCODES.ASTORE, 2);
            tmp.visitVarInsn(OPCODES.ALOAD, 0);
            tmp.visitVarInsn(OPCODES.ALOAD, 2);
            tmp.visitMethodInsn(OPCODES.INVOKESTATIC, 'lain/mods/skins/init/forge/Hooks', 'getLocationCape', '(Lnet/minecraft/client/entity/player/AbstractClientPlayerEntity;Lnet/minecraft/util/ResourceLocation;)Lnet/minecraft/util/ResourceLocation;', false);
            i += tmp.instructions.size();
            node.instructions.insertBefore(insn, tmp.instructions);
        }
    }
}
// net/minecraft/client/entity/player/AbstractClientPlayerEntity/func_110306_p (getLocationSkin)
function transformMethod002(node) {
    for (var i = 0; i < node.instructions.size(); i++) {
        var insn = node.instructions.get(i);
        if (OPCODES.ARETURN === insn.getOpcode()) {
            var tmp = ASMAPI.getMethodNode();
            tmp.visitVarInsn(OPCODES.ASTORE, 2);
            tmp.visitVarInsn(OPCODES.ALOAD, 0);
            tmp.visitVarInsn(OPCODES.ALOAD, 2);
            tmp.visitMethodInsn(OPCODES.INVOKESTATIC, 'lain/mods/skins/init/forge/Hooks', 'getLocationSkin', '(Lnet/minecraft/client/entity/player/AbstractClientPlayerEntity;Lnet/minecraft/util/ResourceLocation;)Lnet/minecraft/util/ResourceLocation;', false);
            i += tmp.instructions.size();
            node.instructions.insertBefore(insn, tmp.instructions);
        }
    }
}
// net/minecraft/client/entity/player/AbstractClientPlayerEntity/func_175154_l (getSkinType)
function transformMethod003(node) {
    for (var i = 0; i < node.instructions.size(); i++) {
        var insn = node.instructions.get(i);
        if (OPCODES.ARETURN === insn.getOpcode()) {
            var tmp = ASMAPI.getMethodNode();
            tmp.visitVarInsn(OPCODES.ASTORE, 2);
            tmp.visitVarInsn(OPCODES.ALOAD, 0);
            tmp.visitVarInsn(OPCODES.ALOAD, 2);
            tmp.visitMethodInsn(OPCODES.INVOKESTATIC, 'lain/mods/skins/init/forge/Hooks', 'getSkinType', '(Lnet/minecraft/client/entity/player/AbstractClientPlayerEntity;Ljava/lang/String;)Ljava/lang/String;', false);
            i += tmp.instructions.size();
            node.instructions.insertBefore(insn, tmp.instructions);
        }
    }
}
// net/minecraft/client/renderer/tileentity/SkullTileEntityRenderer/func_228878_a_
function transformMethod004(node) {
    for (var i = 0; i < node.instructions.size(); i++) {
        var insn = node.instructions.get(i);
        if (OPCODES.INVOKESTATIC === insn.getOpcode() && ('func_228644_e_' === insn.name || 'func_228640_c_' === insn.name)) {
            var tmp = ASMAPI.getMethodNode();
            tmp.visitVarInsn(OPCODES.ASTORE, 2);
            tmp.visitVarInsn(OPCODES.ALOAD, 0);
            tmp.visitVarInsn(OPCODES.ALOAD, 1);
            tmp.visitVarInsn(OPCODES.ALOAD, 2);
            tmp.visitMethodInsn(OPCODES.INVOKESTATIC, 'lain/mods/skins/init/forge/Hooks', 'getLocationSkin_SkullRenderer', '(Lnet/minecraft/block/SkullBlock$ISkullType;Lcom/mojang/authlib/GameProfile;Lnet/minecraft/util/ResourceLocation;)Lnet/minecraft/util/ResourceLocation;', false);
            i += tmp.instructions.size();
            node.instructions.insertBefore(insn, tmp.instructions);
        }
    }
}
// net/minecraft/client/gui/overlay/PlayerTabOverlayGui/func_238523_a_
function transformMethod005(node) {
    for (var i = 0; i < node.instructions.size(); i++) {
        var insn = node.instructions.get(i);
        if (OPCODES.ISTORE === insn.getOpcode() && 12 === insn.var) {
            var tmp = ASMAPI.getMethodNode();
            tmp.visitInsn(OPCODES.POP);
            tmp.visitInsn(OPCODES.ICONST_1);
            i += tmp.instructions.size();
            node.instructions.insertBefore(insn, tmp.instructions);
        } else if (OPCODES.INVOKEVIRTUAL === insn.getOpcode() && 'func_110577_a' === insn.name) { // bindTexture
            var tmp = ASMAPI.getMethodNode();
            tmp.visitVarInsn(OPCODES.ASTORE, 35);
            tmp.visitVarInsn(OPCODES.ALOAD, 27);
            tmp.visitVarInsn(OPCODES.ALOAD, 35);
            tmp.visitMethodInsn(OPCODES.INVOKESTATIC, 'lain/mods/skins/init/forge/Hooks', 'getLocationSkin_TabOverlay', '(Lcom/mojang/authlib/GameProfile;Lnet/minecraft/util/ResourceLocation;)Lnet/minecraft/util/ResourceLocation;', false);
            i += tmp.instructions.size();
            node.instructions.insertBefore(insn, tmp.instructions);
        }
    }
}

function initializeCoreMod() {
    return {
        'Transformer001': {
            'target': {
                'type': 'CLASS',
                'name': 'net/minecraft/client/entity/player/AbstractClientPlayerEntity'
            },
            'transformer': function(node) {
                node.methods.forEach(function(method) {
                    if ('func_110303_q' === method.name)
                        transformMethod001(method);
                    else if ('func_110306_p' === method.name)
                        transformMethod002(method);
                    else if ('func_175154_l' === method.name)
                        transformMethod003(method);
                });
                return node;
            }
        },
        'Transformer002': {
            'target': {
                'type': 'CLASS',
                'name': 'net/minecraft/client/renderer/tileentity/SkullTileEntityRenderer'
            },
            'transformer': function(node) {
                node.methods.forEach(function(method) {
                    if ('func_228878_a_' === method.name)
                        transformMethod004(method);
                });
                return node;
            }
        },
        'Transformer003': {
            'target': {
                'type': 'CLASS',
                'name': 'net/minecraft/client/gui/overlay/PlayerTabOverlayGui'
            },
            'transformer': function(node) {
                node.methods.forEach(function(method) {
                    if ('func_238523_a_' === method.name)
                        transformMethod005(method);
                });
                return node;
            }
        }
    };
}