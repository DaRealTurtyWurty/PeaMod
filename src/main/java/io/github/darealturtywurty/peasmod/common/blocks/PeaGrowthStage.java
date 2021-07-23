package io.github.darealturtywurty.peasmod.common.blocks;

import java.util.Locale;

import io.github.darealturtywurty.peasmod.PeasMod;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.ResourceLocation;

public enum PeaGrowthStage implements IStringSerializable {
	BOTTOM0, BOTTOM1, BOTTOM2, BOTTOM3, BOTTOM4, BOTTOM5, BOTTOM6, TOP0;

	@Override
	public String getSerializedName() {
		return name().toLowerCase(Locale.ENGLISH);
	}

	public ResourceLocation getTextureName() {
		return new ResourceLocation(PeasMod.MODID, "blocks/peas/" + getSerializedName());
	}

	public PeaGrowthStage next() {
		switch (this) {
		case BOTTOM0:
			return BOTTOM1;
		case BOTTOM1:
			return BOTTOM2;
		case BOTTOM2:
			return BOTTOM3;
		case BOTTOM3:
			return BOTTOM4;
		case BOTTOM4:
			return BOTTOM5;
		case BOTTOM5:
			return BOTTOM6;
		case BOTTOM6:
		default:
			return this;
		}
	}

	@Override
	public String toString() {
		return getSerializedName();
	}
}
