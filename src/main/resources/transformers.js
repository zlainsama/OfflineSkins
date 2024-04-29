var ASMAPI = Java.type('net.neoforged.coremod.api.ASMAPI');
var OPCODES = Java.type('org.objectweb.asm.Opcodes');

function transformMethod001(node) {
    for (var i = 0; i < node.instructions.size(); i++) {
        var insn = node.instructions.get(i);
        if (OPCODES.ARETURN === insn.getOpcode()) {
            var tmp = ASMAPI.getMethodNode();
            tmp.visitVarInsn(OPCODES.ASTORE, 2);
            tmp.visitVarInsn(OPCODES.ALOAD, 0);
            tmp.visitVarInsn(OPCODES.ALOAD, 2);
            tmp.visitMethodInsn(OPCODES.INVOKESTATIC, 'lain/mods/skins/init/neoforge/Hooks', 'getSkin', '(Lnet/minecraft/client/multiplayer/PlayerInfo;Lnet/minecraft/client/resources/PlayerSkin;)Lnet/minecraft/client/resources/PlayerSkin;', false);
            i += tmp.instructions.size();
            node.instructions.insertBefore(insn, tmp.instructions);
        }
    }
}
function transformMethod002(node) {
    for (var i = 0; i < node.instructions.size(); i++) {
        var insn = node.instructions.get(i);
        if (OPCODES.INVOKESTATIC === insn.getOpcode() && ('entityTranslucent' === insn.name || ('c' === insn.name && '(Lalf;Z)Lgdx;' === insn.desc))) { // entityTranslucent
            var tmp = ASMAPI.getMethodNode();
            tmp.visitVarInsn(OPCODES.ASTORE, 2);
            tmp.visitVarInsn(OPCODES.ALOAD, 0);
            tmp.visitVarInsn(OPCODES.ALOAD, 1);
            tmp.visitVarInsn(OPCODES.ALOAD, 2);
            tmp.visitMethodInsn(OPCODES.INVOKESTATIC, 'lain/mods/skins/init/neoforge/Hooks', 'getSkinLocation', '(Lnet/minecraft/world/level/block/SkullBlock$Type;Lnet/minecraft/world/item/component/ResolvableProfile;Lnet/minecraft/resources/ResourceLocation;)Lnet/minecraft/resources/ResourceLocation;', false);
            i += tmp.instructions.size();
            node.instructions.insertBefore(insn, tmp.instructions);
        }
    }
}
function transformMethod003(node) {
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
                    if ('getSkin' === method.name || ('g' === method.name && '()Lgqa;' === method.desc))
                        transformMethod001(method);
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
                    if ('getRenderType' === method.name || ('a' === method.name && '(Ldmc$a;Lcxs;)Lgdx;' === method.desc))
                        transformMethod002(method);
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
                    if ('render' === method.name || ('a' === method.name && '(Lfgs;ILeww;Lewo;)V' === method.desc))
                        transformMethod003(method);
                });
                return node;
            }
        }
    };
}