package EmpiresMod.protection.JSON;

import java.io.File;
import java.io.FileReader;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import EmpiresMod.Constants;
import EmpiresMod.Empires;
import EmpiresMod.entities.Flags.FlagType;
import EmpiresMod.entities.Misc.Volume;
import EmpiresMod.protection.ProtectionManager;
import EmpiresMod.protection.Segment.Segment;
import EmpiresMod.protection.Segment.Caller.Caller;
import EmpiresMod.protection.Segment.Getter.Getter;

/**
 * JSON Parser used to parse protection files.
 */
public class ProtectionParser {

	private static final Gson gson = new GsonBuilder().registerTypeAdapter(Caller.class, new Caller.Serializer())
			.registerTypeAdapter(Getter.class, new Getter.Serializer())
			.registerTypeAdapter(Protection.class, new Protection.Serializer())
			.registerTypeAdapter(Segment.class, new Segment.Serializer())
			.registerTypeAdapter(Volume.class, new Volume.Serializer())
			.registerTypeAdapter(FlagType.class, new FlagType.Serializer()).setPrettyPrinting().create();

	private ProtectionParser() {
	}

	public static boolean start() {
		String folderPath = Constants.CONFIG_FOLDER + "Protection Files/";
		File folder = new File(folderPath);
		if (!folder.exists()) {
			if (!folder.mkdir()) {
				return false;
			}
		}

		boolean anyProtectionLoaded = false;
		String[] extensions = new String[1];
		extensions[0] = "json";

		ProtectionManager.segmentsBlock.clear();
		ProtectionManager.segmentsEntity.clear();
		ProtectionManager.segmentsItem.clear();
		ProtectionManager.segmentsTile.clear();

		Protection vanillaProtection = null;
		for (File file : FileUtils.listFiles(folder, extensions, true)) {
			Protection protection = read(file);
			if (protection != null) {
				anyProtectionLoaded = true;
				if ("Minecraft".equals(protection.modid)) {
					vanillaProtection = protection;
				} else {
					Empires.instance.LOG.info("Adding protection for mod: {}",
							protection.name.length() > 0 ? protection.name : protection.modid);
					ProtectionManager.addProtection(protection);
				}
			}
		}
		if (vanillaProtection != null) {
			Empires.instance.LOG.info("Adding vanilla protection.");
			ProtectionManager.addProtection(vanillaProtection);
		}

		if (!anyProtectionLoaded) {
			Empires.instance.LOG.warn(
					"We are built to use similar protection files as MyTown, check out their Github site to add protections: http://github.com/MyEssentials/MyTown2-Protections");
			Empires.instance.LOG.warn(
					"The Server Will Not Have Proper Protection Without Those Files, Continue Without Them At Your Own Risk!");
		}

		return true;
	}

	private static Protection read(File file) {
		try {
			FileReader reader = new FileReader(file);
			Empires.instance.LOG.info("Loading protection file: {}",
			file.getName());
			Protection protection = gson.fromJson(reader, Protection.class);
			reader.close();
			return protection;
		} catch (Exception ex) {
			Empires.instance.LOG.error("Encountered error when parsing protection file: {}", file.getName());
			Empires.instance.LOG.error(ExceptionUtils.getStackTrace(ex));
			return null;
		}
	}
}
