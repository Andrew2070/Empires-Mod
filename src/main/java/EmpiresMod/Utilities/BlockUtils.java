package EmpiresMod.Utilities;

import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraft.block.Block;

public class BlockUtils {
	public static String getBlockName(Block block) {
		return GameRegistry.findUniqueIdentifierFor(block).toString().replace(":", ".");
	}
}