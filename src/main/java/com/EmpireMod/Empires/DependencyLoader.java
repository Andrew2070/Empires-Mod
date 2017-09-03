package com.EmpireMod.Empires;


import cpw.mods.fml.relauncher.FMLInjectionData;
import cpw.mods.fml.relauncher.IFMLCallHook;
import cpw.mods.fml.relauncher.IFMLLoadingPlugin;
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

        new com.EmpireMod.Empires.Dependencies.DependencyLoader(new File(mcDir, "MyEssentials/libs"), (LaunchClassLoader) com.EmpireMod.Empires.Dependencies.DependencyLoader.class.getClassLoader()).load();

        return null;
    }

    @Override
    public String[] getASMTransformerClass() {
        return new String[] {
            "com.EmpireMod.Empires.Transformers.AE2PlaceTransformer",
            "com.EmpireMod.Empires.Transformers.AE2NetworkToolTransformer",
            "com.EmpireMod.Empires.Transformers.BlockFarmlandTransformer",
            "com.EmpireMod.Empires.Transformers.BlockFireTransformer",
            "com.EmpireMod.Empires.Transformers.BlockTaintFibersTransformer",
            "com.EmpireMod.Empires.Transformers.EntityFireballTransformer",
            "com.EmpireMod.Empires.Transformers.EntityThrowableTransformer",
            "com.EmpireMod.Empires.Transformers.SignClassTransformer"
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