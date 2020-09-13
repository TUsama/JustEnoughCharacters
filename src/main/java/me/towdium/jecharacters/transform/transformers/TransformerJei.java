package me.towdium.jecharacters.transform.transformers;

import it.unimi.dsi.fastutil.ints.IntSet;
import mcp.MethodsReturnNonnullByDefault;
import me.towdium.jecharacters.JechConfig;
import me.towdium.jecharacters.core.JechCore;
import me.towdium.jecharacters.match.PinyinTree;
import me.towdium.jecharacters.transform.Transformer;
import mezz.jei.suffixtree.GeneralizedSuffixTree;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodInsnNode;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Author: Towdium
 * Date:   13/06/17
 */

public class TransformerJei extends Transformer.Default {
    @SuppressWarnings("unused")
    public static String wrap(String s) {
        return "\"" + s + "\"";
    }

    @Override
    public boolean accepts(String name) {
        return JechConfig.enableJEI && name.equals("mezz.jei.ingredients.IngredientFilter");
    }

    @Override
    public void transform(ClassNode n) {
        JechCore.LOG.info("Transforming class " + n.name + " for JEI integration.");
        Transformer.findMethod(n, "<init>").ifPresent(methodNode ->
                Transformer.transformConstruct(methodNode, "mezz/jei/suffixtree/GeneralizedSuffixTree",
                        "me/towdium/jecharacters/transform/transformers/TransformerJei$FakeTree"));
        Transformer.findMethod(n, "createPrefixedSearchTree").ifPresent(methodNode ->
                Transformer.transformConstruct(methodNode, "mezz/jei/suffixtree/GeneralizedSuffixTree",
                        "me/towdium/jecharacters/transform/transformers/TransformerJei$FakeTree"));
        if (JechConfig.enableForceQuote) Transformer.findMethod(n, "getElements").ifPresent(methodNode -> {
            InsnList list = methodNode.instructions;
            list.insert(list.get(3), new MethodInsnNode(Opcodes.INVOKESTATIC,
                    "me/towdium/jecharacters/transform/transformers/TransformerJei", "wrap",
                    "(Ljava/lang/String;)Ljava/lang/String;", false));
        });
    }

    @ParametersAreNonnullByDefault
    @MethodsReturnNonnullByDefault
    public static class FakeTree extends GeneralizedSuffixTree {
        PinyinTree graph = new PinyinTree();
        int highestIndex = -1;

        public IntSet search(String word) {
            return graph.search(word);
        }

        public void put(String key, int index) throws IllegalStateException {
            if (index < highestIndex) {
                String err = "The input index must not be less than any of the previously " +
                        "inserted ones. Got " + index + ", expected at least " + highestIndex;
                throw new IllegalStateException(err);
            } else highestIndex = index;
            graph.put(key, index);
        }

        @Override
        public int getHighestIndex() {
            return highestIndex;
        }
    }
}