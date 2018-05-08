package EmpiresMod.entities.Empire;

public class AdminEmpire extends Empire {
	public AdminEmpire(String name) {
		super(name);
	}

	@Override
	public int getMaxBlocks() {
		return Integer.MAX_VALUE;
	}
}