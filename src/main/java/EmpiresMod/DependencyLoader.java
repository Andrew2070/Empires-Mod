package EmpiresMod;

import net.minecraftforge.fml.relauncher.FMLInjectionData;
import net.minecraftforge.fml.relauncher.IFMLCallHook;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;

import net.minecraft.launchwrapper.LaunchClassLoader;

import java.io.File;
import java.util.Map;

/**
 * For autodownloading stuff.
 * This is really unoriginal, mostly ripped off FML, credits to cpw and chicken-bones.
 */
@IFMLLoadingPlugin.SortingIndex(1001)
public class DependencyLoader implements IFMLLoadingPlugin, IFMLCallHook {
    @Override
    public Void call() throws Exception {
        File mcDir = (File) FMLInjectionData.data()[6];

        new EmpiresMod.Dependencies.DependencyLoader(new File(mcDir, "Empires-Mod/libs"), (LaunchClassLoader) EmpiresMod.Dependencies.DependencyLoader.class.getClassLoader()).load();

        return null;
    }

    @Override
    public String[] getASMTransformerClass() {
        return new String[] {
            "EmpiresMod.Transformers.AE2PlaceTransformer",
            "EmpiresMod.Transformers.AE2NetworkToolTransformer",
            "EmpiresMod.Transformers.BlockFarmlandTransformer",
            "EmpiresMod.Transformers.BlockFireTransformer",
            "EmpiresMod.Transformers.BlockTaintFibersTransformer",
            "EmpiresMod.Transformers.EntityFireballTransformer",
            "EmpiresMod.Transformers.EntityThrowableTransformer",
            "EmpiresMod.Transformers.SignClassTransformer"
        };
    }

    @Override
    public String getModContainerClass() {
        return null;
    }

    @Override
    public String getSetupClass() {
        return getClass().getName();
    }

    @Override
    public void injectData(Map<String, Object> data) {
    }

    @Override
    public String getAccessTransformerClass() {
        return null;
    }
}