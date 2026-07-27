package com.oritong.oritongsdiamondcore;

import net.minecraft.resources.ResourceLocation;

import net.minecraftforge.fml.common.Mod;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(OritongsDiamondCore.MODID)
public class OritongsDiamondCore {
    public static final String MODID = "oritongsdiamondcore";
    private static final Logger LOGGER = LogManager.getLogger();

    public OritongsDiamondCore() {
        LOGGER.info("Oritong's Diamond Core loaded");
    }

    public static ResourceLocation id(String name) {
        return new ResourceLocation(MODID, name);
    }
}
