var Opcodes = Java.type('org.objectweb.asm.Opcodes');
var InsnList = Java.type("org.objectweb.asm.tree.InsnList");
var InsnNode = Java.type('org.objectweb.asm.tree.InsnNode');
var VarInsnNode = Java.type('org.objectweb.asm.tree.VarInsnNode');
var FieldInsnNode = Java.type('org.objectweb.asm.tree.FieldInsnNode');
var MethodInsnNode = Java.type('org.objectweb.asm.tree.MethodInsnNode');
var TypeInsnNode = Java.type('org.objectweb.asm.tree.TypeInsnNode');
var JumpInsnNode = Java.type('org.objectweb.asm.tree.JumpInsnNode');
var LdcInsnNode = Java.type('org.objectweb.asm.tree.LdcInsnNode');
var InvokeDynamicInsnNode = Java.type('org.objectweb.asm.tree.InvokeDynamicInsnNode');
var LabelNode = Java.type('org.objectweb.asm.tree.LabelNode');
var FrameNode = Java.type('org.objectweb.asm.tree.FrameNode');

function initializeCoreMod() {
    return {

        'forge_config_spec_patch': {
            'target': {
                'type': 'CLASS',
                'name': 'net.minecraftforge.common.ForgeConfigSpec'
            },
            'transformer': function(classNode) {
                patchMethod([{
                    obfName: "correct",
                    name: "correct",
                    desc: "(Lcom/electronwill/nightconfig/core/UnmodifiableConfig;Lcom/electronwill/nightconfig/core/CommentedConfig;Ljava/util/LinkedList;Ljava/util/List;Lcom/electronwill/nightconfig/core/ConfigSpec$CorrectionListener;Lcom/electronwill/nightconfig/core/ConfigSpec$CorrectionListener;Z)I",
                    patches: [patchCorrect]
                }], classNode, "ForgeConfigSpec");
                return classNode;
            }
        }
    };
}

function patchMethod(entries, classNode, name) {

    log("Patching " + name + "...");
    for (var i = 0; i < entries.length; i++) {

        var entry = entries[i];
        var method = findMethod(classNode.methods, entry);
        var flag = !!method;
        if (flag) {

            var obfuscated = !method.name.equals(entry.name);
            for (var j = 0; j < entry.patches.length; j++) {

                var patch = entry.patches[j];
                if (!patchInstructions(method, patch.filter, patch.action, obfuscated)) {

                    flag = false;
                }
            }
        }

        log("Patching " + name + "#" + entry.name + (flag ? " was successful" : " failed"));
    }
}

function findMethod(methods, entry) {

    for (var i = 0; i < methods.length; i++) {

        var method = methods[i];
        if ((method.name.equals(entry.obfName) || method.name.equals(entry.name)) && method.desc.equals(entry.desc)) {

            return method;
        }
    }
}

function patchInstructions(method, filter, action, obfuscated) {

    var instructions = method.instructions.toArray();
    for (var i = 0; i < instructions.length; i++) {

        var node = filter(instructions[i], obfuscated);
        if (!!node) {

            break;
        }
    }

    if (!!node) {

        action(node, method.instructions, obfuscated);
        return true;
    }
}

var patchCorrect = {
    filter: function(node, obfuscated) {
        // insert at head
        return node
    },
    action: function(node, instructions, obfuscated) {
        var insnList = new InsnList();
        insnList.add(new VarInsnNode(Opcodes.ALOAD, 1))
        insnList.add(new VarInsnNode(Opcodes.ALOAD, 2))
        insnList.add(new VarInsnNode(Opcodes.ALOAD, 3))
        insnList.add(new VarInsnNode(Opcodes.ALOAD, 4))
        insnList.add(new VarInsnNode(Opcodes.ALOAD, 5))
        insnList.add(new VarInsnNode(Opcodes.ALOAD, 6))
        insnList.add(new VarInsnNode(Opcodes.ILOAD, 7))
        insnList.add(new VarInsnNode(Opcodes.ALOAD, 0))
        insnList.add(new FieldInsnNode(Opcodes.GETFIELD, "net/minecraftforge/common/ForgeConfigSpec", "levelComments", "Ljava/util/Map;"))
        insnList.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "fuzs/nightconfigfixes/config/ConfigSpecWrapper", "correct", "(Lcom/electronwill/nightconfig/core/UnmodifiableConfig;Lcom/electronwill/nightconfig/core/CommentedConfig;Ljava/util/LinkedList;Ljava/util/List;Lcom/electronwill/nightconfig/core/ConfigSpec$CorrectionListener;Lcom/electronwill/nightconfig/core/ConfigSpec$CorrectionListener;ZLjava/util/Map;)I", false))
        insnList.add(new InsnNode(Opcodes.IRETURN))
        instructions.insert(insnList)
    }
};

function matchesNode(node, owner, name, desc) {

    return node instanceof MethodInsnNode && node.owner.equals(owner) && node.name.equals(name) && node.desc.equals(desc);
}

function getNthNode(node, n) {

    for (var i = 0; i < Math.abs(n); i++) {

        if (n < 0) {

            node = node.getPrevious();
        } else {

            node = node.getNext();
        }
    }

    return node;
}

function log(message) {

    print("[Night Config Fixes Transformer]: " + message);
}