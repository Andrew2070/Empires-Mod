package EmpiresMod.Transformers;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.GeneratorAdapter;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;

/**
 * Patches ToolNetworkTool to make it fire a {@link PlayerInteractEvent} before
 * executing any action <br/>
 * The final code would be:
 * 
 * <pre>
 * <code>
 *     public class ToolNetworkTool extends AEBaseItem implements IGuiItem, IAEWrench {
 *         // ... original fields and methods
 *
 *         public boolean serverSideToolLogic(ItemStack is, EntityPlayer p, World w, int x, int y, int z, int side, float hitX, float hitY, float hitZ) {
 *             if(ToolNetworkToolCT.onBlockActivated(w, x, y, z, p, side, hitX, hitY, hitZ))
 *                 return false;
 *             // ... original code
 *         }
 *     }
 * </code>
 * </pre>
 */
public class AE2NetworkToolTransformer implements IClassTransformer {
	public static boolean onItemUse(IBlockState iBlockState, World world, int x, int y, int z, EntityPlayer player, int side,
			float hitX, float hitY, float hitZ) {
		
		return MinecraftForge.EVENT_BUS.post(new PlayerInteractEvent(player, side == -1 ? PlayerInteractEvent.Action.RIGHT_CLICK_AIR : PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK,
				new BlockPos(x,y,z), EnumFacing.getFacingFromVector((float)player.getLookVec().xCoord, (float)player.getLookVec().yCoord,(float) player.getLookVec().zCoord), world));
		
	}

	public static boolean onItemUse(World world, int x, int y, int z, EntityPlayer player, int side, float hitX,
			float hitY, float hitZ) {
		return onItemUse(world.getBlockState(new BlockPos(x, y, z)), world, x, y, z, player, side, hitX, hitY, hitZ);
	}

	private class Generator extends GeneratorAdapter {
		boolean patched;

		protected Generator(MethodVisitor mv, int access, String name, String desc) {
			super(Opcodes.ASM4, mv, access, name, desc);
		}

		@Override
		public void visitVarInsn(int opcode, int var) {
			if (patched) {
				super.visitVarInsn(opcode, var);
				return;
			}

			super.visitVarInsn(Opcodes.ALOAD, 3);
			super.visitVarInsn(Opcodes.ILOAD, 4);
			super.visitVarInsn(Opcodes.ILOAD, 5);
			super.visitVarInsn(Opcodes.ILOAD, 6);
			super.visitVarInsn(Opcodes.ALOAD, 2);
			super.visitVarInsn(Opcodes.ILOAD, 7);
			super.visitVarInsn(Opcodes.FLOAD, 8);
			super.visitVarInsn(Opcodes.FLOAD, 9);
			super.visitVarInsn(Opcodes.FLOAD, 10);
			super.visitMethodInsn(Opcodes.INVOKESTATIC, "EmpiresMod/Transformers/AE2NetworkToolTransformer",
					"onItemUse", "(Lnet/minecraft/world/World;IIILnet/minecraft/entity/player/EntityPlayer;IFFF)Z");
			Label allowed = new Label();
			super.visitJumpInsn(Opcodes.IFEQ, allowed);
			super.visitInsn(Opcodes.ICONST_0);
			super.visitInsn(Opcodes.IRETURN);
			super.visitLabel(allowed);

			super.visitVarInsn(opcode, var);
			patched = true;
		}
	}

	@Override
	public byte[] transform(String name, String srgName, byte[] bytes) {
		if ("appeng.items.tools.ToolNetworkTool".equals(srgName)) {
			ClassReader reader = new ClassReader(bytes);
			ClassWriter writer = new ClassWriter(reader, Opcodes.ASM4);

			ClassVisitor visitor = new ClassVisitor(Opcodes.ASM4, writer) {
				@Override
				public MethodVisitor visitMethod(int access, String name, String desc, String signature,
						String[] exceptions) {
					MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);

					if ("serverSideToolLogic".equals(name)) {
						return new Generator(mv, access, name, desc);
					}

					return mv;
				}
			};

			reader.accept(visitor, ClassReader.EXPAND_FRAMES);

			bytes = writer.toByteArray();
		}

		return bytes;
	}
}