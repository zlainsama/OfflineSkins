var ASMAPI = Java.type('net.minecraftforge.coremod.api.ASMAPI');
var OPCODES = Java.type('org.objectweb.asm.Opcodes');

// net/minecraft/client/multiplayer/PlayerInfo/m_105337_ (getSkinLocation)
function transformMethod001(node) {
    for (var i = 0; i < node.instructions.size(); i++) {
        var insn = node.instructions.get(i);
        if (OPCODES.ARETURN === insn.getOpcode()) {
            var tmp = ASMAPI.getMethodNode();
            tmp.visitVarInsn(OPCODES.ASTORE, 2);
            tmp.visitVarInsn(OPCODES.ALOAD, 0);
            tmp.visitVarInsn(OPCODES.ALOAD, 2);
            tmp.visitMethodInsn(OPCODES.INVOKESTATIC, 'lain/mods/skins/init/forge/Hooks', 'getSkinLocation', '(Lnet/minecraft/client/multiplayer/PlayerInfo;Lnet/minecraft/resources/ResourceLocation;)Lnet/minecraft/resources/ResourceLocation;', false);
            i += tmp.instructions.size();
            node.instructions.insertBefore(insn, tmp.instructions);
        }
    }
}
// net/minecraft/client/multiplayer/PlayerInfo/m_105338_ (getCapeLocation)
function transformMethod002(node) {
    for (var i = 0; i < node.instructions.size(); i++) {
        var insn = node.instructions.get(i);
        if (OPCODES.ARETURN === insn.getOpcode()) {
            var tmp = ASMAPI.getMethodNode();
            tmp.visitVarInsn(OPCODES.ASTORE, 2);
            tmp.visitVarInsn(OPCODES.ALOAD, 0);
            tmp.visitVarInsn(OPCODES.ALOAD, 2);
            tmp.visitMethodInsn(OPCODES.INVOKESTATIC, 'lain/mods/skins/init/forge/Hooks', 'getCapeLocation', '(Lnet/minecraft/client/multiplayer/PlayerInfo;Lnet/minecraft/resources/ResourceLocation;)Lnet/minecraft/resources/ResourceLocation;', false);
            i += tmp.instructions.size();
            node.instructions.insertBefore(insn, tmp.instructions);
        }
    }
}
// net/minecraft/client/multiplayer/PlayerInfo/m_105336_ (getModelName)
function transformMethod003(node) {
    for (var i = 0; i < node.instructions.size(); i++) {
        var insn = node.instructions.get(i);
        if (OPCODES.ARETURN === insn.getOpcode()) {
            var tmp = ASMAPI.getMethodNode();
            tmp.visitVarInsn(OPCODES.ASTORE, 2);
            tmp.visitVarInsn(OPCODES.ALOAD, 0);
            tmp.visitVarInsn(OPCODES.ALOAD, 2);
            tmp.visitMethodInsn(OPCODES.INVOKESTATIC, 'lain/mods/skins/init/forge/Hooks', 'getModelName', '(Lnet/minecraft/client/multiplayer/PlayerInfo;Ljava/lang/String;)Ljava/lang/String;', false);
            i += tmp.instructions.size();
            node.instructions.insertBefore(insn, tmp.instructions);
        }
    }
}
// net/minecraft/client/renderer/blockentity/SkullBlockRenderer/m_112523_ (getRenderType)
function transformMethod004(node) {
    for (var i = 0; i < node.instructions.size(); i++) {
        var insn = node.instructions.get(i);
        if (OPCODES.INVOKESTATIC === insn.getOpcode() && ('m_110473_' === insn.name || 'm_110458_' === insn.name)) { // entityTranslucent | entityCutoutNoCull
            var tmp = ASMAPI.getMethodNode();
            tmp.visitVarInsn(OPCODES.ASTORE, 2);
            tmp.visitVarInsn(OPCODES.ALOAD, 0);
            tmp.visitVarInsn(OPCODES.ALOAD, 1);
            tmp.visitVarInsn(OPCODES.ALOAD, 2);
            tmp.visitMethodInsn(OPCODES.INVOKESTATIC, 'lain/mods/skins/init/forge/Hooks', 'getSkinLocation', '(Lnet/minecraft/world/level/block/SkullBlock$Type;Lcom/mojang/authlib/GameProfile;Lnet/minecraft/resources/ResourceLocation;)Lnet/minecraft/resources/ResourceLocation;', false);
            i += tmp.instructions.size();
            node.instructions.insertBefore(insn, tmp.instructions);
        }
    }
}
// net/minecraft/client/gui/components/PlayerTabOverlay/m_94544_ (render)
function transformMethod005(node) {
    for (var i = 0; i < node.instructions.size(); i++) {
        var insn = node.instructions.get(i);
        if (OPCODES.ISTORE === insn.getOpcode() && 12 === insn.var) {
            var tmp = ASMAPI.getMethodNode();
            tmp.visitInsn(OPCODES.POP);
            tmp.visitInsn(OPCODES.ICONST_1);
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
                'name': 'net/minecraft/client/multiplayer/PlayerInfo'
            },
            'transformer': function(node) {
                node.methods.forEach(function(method) {
                    if ('m_105337_' === method.name)
                        transformMethod001(method);
                    else if ('m_105338_' === method.name)
                        transformMethod002(method);
                    else if ('m_105336_' === method.name)
                        transformMethod003(method);
                });
                return node;
            }
        },
        'Transformer002': {
            'target': {
                'type': 'CLASS',
                'name': 'net/minecraft/client/renderer/blockentity/SkullBlockRenderer'
            },
            'transformer': function(node) {
                node.methods.forEach(function(method) {
                    if ('m_112523_' === method.name)
                        transformMethod004(method);
                });
                return node;
            }
        },
        'Transformer003': {
            'target': {
                'type': 'CLASS',
                'name': 'net/minecraft/client/gui/components/PlayerTabOverlay'
            },
            'transformer': function(node) {
                node.methods.forEach(function(method) {
                    if ('m_94544_' === method.name)
                        transformMethod005(method);
                });
                return node;
            }
        }
    };
}