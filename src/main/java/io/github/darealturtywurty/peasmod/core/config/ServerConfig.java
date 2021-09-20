package io.github.darealturtywurty.peasmod.core.config;

import org.apache.commons.lang3.tuple.Pair;

import io.github.darealturtywurty.peasmod.PeasMod;
import net.minecraftforge.common.ForgeConfigSpec;

public class ServerConfig {

	public static final ServerConfig SERVER_CONF;
	public static final ForgeConfigSpec SERVER_SPEC;

	static {
		final Pair<ServerConfig, ForgeConfigSpec> specConfig = new ForgeConfigSpec.Builder().configure(ServerConfig::new);
		SERVER_SPEC = specConfig.getRight();
		SERVER_CONF = specConfig.getLeft();
	}

	public static float frozenPeasHealAmount;

	public static void refreshServer() {
		frozenPeasHealAmount = SERVER_CONF.frozenPeasHealAmountVal.get();
	}

	public final ForgeConfigSpec.ConfigValue<Float> frozenPeasHealAmountVal;

	ServerConfig(final ForgeConfigSpec.Builder builder) {
		builder.push("general");
		this.frozenPeasHealAmountVal = builder.comment("Amount of health that frozen peas will heal for.")
				.translation("text." + PeasMod.MODID + ".config.frozen_peas").define("frozenPeasHealAmount", 5f);
		builder.pop();
	}
}
