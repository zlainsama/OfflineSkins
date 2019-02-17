var InsnNode = Java.type('org.objectweb.asm.tree.InsnNode');
var VarInsnNode = Java.type('org.objectweb.asm.tree.VarInsnNode');
var MethodInsnNode = Java.type('org.objectweb.asm.tree.MethodInsnNode');
var InsnList = Java.type('org.objectweb.asm.tree.InsnList');

// net/minecraft/client/entity/AbstractClientPlayer/func_110303_q (getLocationCape)
function transformMethod001(node) {
    for (var i = 0; i < node.instructions.size(); i++) {
        var insn = node.instructions.get(i);
        if (176 === insn.getOpcode()) { // ARETURN
            var toInsert = new InsnList();
            toInsert.add(new VarInsnNode(58, 2)); // ASTORE
            toInsert.add(new VarInsnNode(25, 0)); // ALOAD
            toInsert.add(new VarInsnNode(25, 2)); // ALOAD
            toInsert.add(new MethodInsnNode(184, 'lain/mods/skins/init/forge/Hooks', 'getLocationCape', '(Lnet/minecraft/client/entity/AbstractClientPlayer;Lnet/minecraft/util/ResourceLocation;)Lnet/minecraft/util/ResourceLocation;', false)); // INVOKESTATIC
            i += toInsert.size();
            node.instructions.insertBefore(insn, toInsert);
        }
    }
}
// net/minecraft/client/entity/AbstractClientPlayer/func_110306_p (getLocationSkin)
function transformMethod002(node) {
    for (var i = 0; i < node.instructions.size(); i++) {
        var insn = node.instructions.get(i);
        if (176 === insn.getOpcode()) { // ARETURN
            var toInsert = new InsnList();
            toInsert.add(new VarInsnNode(58, 2)); // ASTORE
            toInsert.add(new VarInsnNode(25, 0)); // ALOAD
            toInsert.add(new VarInsnNode(25, 2)); // ALOAD
            toInsert.add(new MethodInsnNode(184, 'lain/mods/skins/init/forge/Hooks', 'getLocationSkin', '(Lnet/minecraft/client/entity/AbstractClientPlayer;Lnet/minecraft/util/ResourceLocation;)Lnet/minecraft/util/ResourceLocation;', false)); // INVOKESTATIC
            i += toInsert.size();
            node.instructions.insertBefore(insn, toInsert);
        }
    }
}
// net/minecraft/client/entity/AbstractClientPlayer/func_175154_l (getSkinType)
function transformMethod003(node) {
    for (var i = 0; i < node.instructions.size(); i++) {
        var insn = node.instructions.get(i);
        if (176 === insn.getOpcode()) { // ARETURN
            var toInsert = new InsnList();
            toInsert.add(new VarInsnNode(58, 2)); // ASTORE
            toInsert.add(new VarInsnNode(25, 0)); // ALOAD
            toInsert.add(new VarInsnNode(25, 2)); // ALOAD
            toInsert.add(new MethodInsnNode(184, 'lain/mods/skins/init/forge/Hooks', 'getSkinType', '(Lnet/minecraft/client/entity/AbstractClientPlayer;Ljava/lang/String;)Ljava/lang/String;', false)); // INVOKESTATIC
            i += toInsert.size();
            node.instructions.insertBefore(insn, toInsert);
        }
    }
}
// net/minecraft/client/renderer/tileentity/TileEntitySkullRenderer/func_199356_a
function transformMethod004(node) {
    for (var i = 0; i < node.instructions.size(); i++) {
        var insn = node.instructions.get(i);
        if (176 === insn.getOpcode()) { // ARETURN
            var toInsert = new InsnList();
            toInsert.add(new VarInsnNode(58, 3)); // ASTORE
            toInsert.add(new VarInsnNode(25, 1)); // ALOAD
            toInsert.add(new VarInsnNode(25, 2)); // ALOAD
            toInsert.add(new VarInsnNode(25, 3)); // ALOAD
            toInsert.add(new MethodInsnNode(184, 'lain/mods/skins/init/forge/Hooks', 'getLocationSkin_SkullRenderer', '(Lnet/minecraft/block/BlockSkull$ISkullType;Lcom/mojang/authlib/GameProfile;Lnet/minecraft/util/ResourceLocation;)Lnet/minecraft/util/ResourceLocation;', false)); // INVOKESTATIC
            i += toInsert.size();
            node.instructions.insertBefore(insn, toInsert);
        }
    }
}
// net/minecraft/client/gui/GuiPlayerTabOverlay/func_175249_a (renderPlayerlist)
function transformMethod005(node) {
    for (var i = 0; i < node.instructions.size(); i++) {
        var insn = node.instructions.get(i);
        if (54 === insn.getOpcode() && 11 === insn.var) { // ISTORE
            var toInsert = new InsnList();
            toInsert.add(new InsnNode(87)); // POP
            toInsert.add(new InsnNode(4)); // ICONST_1
            i += toInsert.size();
            node.instructions.insertBefore(insn, toInsert);
        } else if (182 === insn.getOpcode() && 'func_110577_a' === insn.name) { // INVOKEVIRTUAL (bindTexture)
            var toInsert = new InsnList();
            toInsert.add(new VarInsnNode(58, 32)); // ASTORE
            toInsert.add(new VarInsnNode(25, 25)); // ALOAD
            toInsert.add(new VarInsnNode(25, 32)); // ALOAD
            toInsert.add(new MethodInsnNode(184, 'lain/mods/skins/init/forge/Hooks', 'getLocationSkin_TabOverlay', '(Lcom/mojang/authlib/GameProfile;Lnet/minecraft/util/ResourceLocation;)Lnet/minecraft/util/ResourceLocation;', false)); // INVOKESTATIC
            i += toInsert.size();
            node.instructions.insertBefore(insn, toInsert);
        }
    }
}

function initializeCoreMod() {
    return {
        'Transformer001': {
            'target': {
                'type': 'CLASS',
                'name': 'net/minecraft/client/entity/AbstractClientPlayer'
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
                'name': 'net/minecraft/client/renderer/tileentity/TileEntitySkullRenderer'
            },
            'transformer': function(node) {
                node.methods.forEach(function(method) {
                    if ('func_199356_a' === method.name)
                        transformMethod004(method);
                });
                return node;
            }
        },
        'Transformer003': {
            'target': {
                'type': 'CLASS',
                'name': 'net/minecraft/client/gui/GuiPlayerTabOverlay'
            },
            'transformer': function(node) {
                node.methods.forEach(function(method) {
                    if ('func_175249_a' === method.name)
                        transformMethod005(method);
                });
                return node;
            }
        }
    };
}