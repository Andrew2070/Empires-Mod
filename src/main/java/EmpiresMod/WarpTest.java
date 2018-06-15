package EmpiresMod;

import EmpiresMod.API.Commands.Command.CommandsEMP;
import EmpiresMod.Misc.Teleport.Teleport;
import EmpiresMod.entities.Empire.Citizen;
import EmpiresMod.entities.Empire.Empire;
import EmpiresMod.entities.Empire.EmpireBlock;

public class WarpTest {
	
	public static Teleport filterWarp(Empire empire, Teleport Warp) {
		for (Teleport warp : empire.Warps) {
			if (warp != null) {
				if (Warp != null) {	
					if (!warp.getEmpire().getName().equals(Warp.getEmpire().getName())) { //check Empire
						if(warp.getName().equals(Warp.getName())) { //check Name
							if(distance(empire, warp) < distance(empire, Warp)) { //check distance
								EmpireBlock knownblock = CommandsEMP.getBlockFromPoint(warp.getDim(), warp.getX(), warp.getZ());
								System.out.println( "left" + distance(empire, warp));
								System.out.println( "Right" + distance( empire, warp));
								return warp;
							
							}

							if(warp.getName().equals(Warp.getName())) {
								if(distance(empire, warp) > distance(empire, Warp)) { //check distance
									return warp;
							}
						}
						}
						}
					}
				}
			}
		return null;
	}
	public static Teleport advfilterWarp(Empire empire, Teleport Warp) {
		
		
		return null;
	}
	public static double distance(Empire empire, Teleport warp) {
		
		float spawnX = empire.getSpawn().getX();
		float spawnY = empire.getSpawn().getY();
		float spawnZ = empire.getSpawn().getZ();
		
		float warpX = warp.getX();
		float warpY = warp.getY();
		float warpZ = warp.getZ();
		
		double powers = Math.pow((warpX-spawnX), 2) + Math.pow((warpY-spawnY), 2) + Math.pow((warpZ-spawnZ), 2);
		double equation = Math.sqrt(powers);
	
		return equation;

	}
	
	
	
	
	
	
	
	
}
