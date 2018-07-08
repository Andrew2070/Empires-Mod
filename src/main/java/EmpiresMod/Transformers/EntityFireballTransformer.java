package EmpiresMod.Transformers;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.GeneratorAdapter;

import net.minecraft.launchwrapper.IClassTransformer;

/**
 * Patches EntityFireball to add a hook for the
 * {@link EmpiresMod.Events.ProjectileImpactEvent}. <br/>
 * The final code would be:
 * 
 * <pre>
 * <code>public class EntityFireball extends Entity {
 *         // ... original fields and methods
 *         public void onUpdate() {
 *             // ... original code
 *             if(ProjectileImpactEvent.fireEvent(this, movingobjectposition)) {
 *               this.onImpact(movingobjectposition);
 *             }
 *             // ... original code
 *         }
 *         // ... original methods
 *     }
 * </code>
 * </pre>
 */
public class EntityFireballTransformer implements IClassTransformer {

	/**
	 * Generates code on the first frame that comes after the first RETURN.<br>
	 * The code that is generated is:
	 * 
	 * <pre>
	 * <code>if(ProjectileImpactEvent.fireEvent(this, movingobjectposition)) this.onImpact(movingobjectposition);</code>
	 * </pre>
	 */
	private class EntityFireballGeneratorAdapter extends GeneratorAdapter {
		boolean patched = false;

		protected EntityFireballGeneratorAdapter(MethodVisitor mv, int access, String name, String desc) {
			super(Opcodes.ASM4, mv, access, name, desc);
		}

		@Override
		public void visitMethodInsn(int opcode, String owner, String name, String desc) {
			if (!patched && opcode == Opcodes.INVOKEVIRTUAL
					&& owner.equals("net/minecraft/entity/projectile/EntityFireball")
					&& (name.equals("func_70227_a") || name.equals("onImpact"))) {
				super.visitMethodInsn(Opcodes.INVOKESTATIC, "EmpiresMod/Events/ProjectileImpactEvent", "fireEvent",
						"(Lnet/minecraft/entity/projectile/EntityFireball;Lnet/minecraft/util/MovingObjectPosition;)Z");
				Label elseLabel = new Label();
				super.visitJumpInsn(Opcodes.IFEQ, elseLabel);
				super.visitVarInsn(Opcodes.ALOAD, 0);
				super.visitVarInsn(Opcodes.ALOAD, 3);
				super.visitMethodInsn(opcode, owner, name, desc);
				super.visitLabel(elseLabel);
				patched = true;
			} else {
				super.visitMethodInsn(opcode, owner, name, desc);
			}
		}
	}

	@Override
	public byte[] transform(String name, String srgName, byte[] bytes) {
		if ("net.minecraft.entity.projectile.EntityFireball".equals(srgName)) {
			ClassReader reader = new ClassReader(bytes);
			ClassWriter writer = new ClassWriter(reader, Opcodes.ASM4);

			ClassVisitor visitor = new ClassVisitor(Opcodes.ASM4, writer) {
				@Override
				public MethodVisitor visitMethod(int access, String name, String desc, String signature,
						String[] exceptions) {
					MethodVisitor methodVisitor = super.visitMethod(access, name, desc, signature, exceptions);

					if ("func_70071_h_".equals(name) || "onUpdate".equals(name))
						return new EntityFireballGeneratorAdapter(methodVisitor, access, name, desc);

					return methodVisitor;
				}
			};

			reader.accept(visitor, ClassReader.EXPAND_FRAMES);

			bytes = writer.toByteArray();
		}

		return bytes;
	}
}