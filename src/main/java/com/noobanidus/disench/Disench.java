package com.noobanidus.disench;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.common.event.FMLLoadCompleteEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import net.minecraftforge.registries.ForgeRegistry;
import net.minecraftforge.registries.GameData;
import net.minecraftforge.registries.RegistryManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Field;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Mod.EventBusSubscriber
@Mod(modid = Disench.MODID, name = Disench.MODNAME, version = Disench.VERSION)
@SuppressWarnings("WeakerAccess")
public class Disench {
  public static final String MODID = "disench";
  public static final String MODNAME = "Enchantment Disabler";
  public static final String VERSION = "GRADLE:VERSION";

  public static Logger LOG;

  @Mod.EventHandler
  public static void preInit(FMLPreInitializationEvent event) {
    LOG = event.getModLog();
  }

  @Mod.EventHandler
  public static void loadComplete(FMLLoadCompleteEvent event) {
    List<ResourceLocation> toRemove = Stream.of(DisenchConfig.enchantments).map(s -> {
      String[] split = s.split(":");
      return new ResourceLocation(split[0], split[1]);
    }).collect(Collectors.toList());
    ForgeRegistry<Enchantment> registry = RegistryManager.ACTIVE.getRegistry(GameData.ENCHANTMENTS);
    Field locked = ReflectionHelper.findField(registry.getClass(), "isModifiable");
    locked.setAccessible(true);
    try {
      locked.set(registry, true);
    } catch (IllegalAccessException e) {
      return;
    }
    toRemove.forEach(rl -> {
      if (registry.remove(rl) != null) {
        LOG.info("Removed enchantment: " + rl.toString());
      }
    });
    try {
      locked.set(registry, false);
    } catch (IllegalAccessException e) {
      return;
    }
    locked.setAccessible(false);
    LOG.info("Enchantment Disabler has done its evil deeds...");
  }

  @Config(modid = MODID)
  public static class DisenchConfig {
    @Config.Comment("List of enchantment registry names to remove")
    @Config.Name("Enchantments to Remove")
    @Config.RequiresMcRestart
    public static String[] enchantments = new String[]{};
  }
}
