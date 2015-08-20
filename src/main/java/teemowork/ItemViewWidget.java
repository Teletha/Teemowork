/*
 * Copyright (C) 2015 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package teemowork;

import static teemowork.ItemViewStyle.*;
import static teemowork.model.Status.*;

import java.util.List;

import jsx.ui.VirtualStructure;
import jsx.ui.Widget;
import jsx.ui.Widget1;
import teemowork.model.Ability;
import teemowork.model.AbilityDescriptor;
import teemowork.model.DescriptionViewWidget;
import teemowork.model.Item;
import teemowork.model.ItemDescriptor;
import teemowork.model.Status;
import teemowork.model.Version;

/**
 * @version 2015/08/19 13:16:22
 */
public class ItemViewWidget extends Widget1<Item> {

    private static final Status[] VISIBLE = {Health, Hreg, Mana, Mreg, AD, ASRatio, ARPen, LS, Critical, AP, CDR, SV, MRPen, AR, MR,
            MSRatio, GoldPer10Sec};

    /**
     * {@inheritDoc}
     */
    @Override
    protected void virtualize(VirtualStructure 〡) {
        ItemDescriptor descriptor = model1.getDescriptor(Version.Latest);

        〡.nbox.ⅼ(Root, () -> {
            〡.nbox.ⅼ(IconArea, () -> {
                〡.nbox.ⅼ(Icon.of(model1));
                〡.nbox.ⅼ(Materials, descriptor.getBuildItem(), material -> {
                    〡.nbox.ⅼ(Material.of(material));
                });
            });
            〡.nbox.ⅼ(DescriptionArea, () -> {
                // Name and Cost
                double cost = model1.getBaseCost();
                double total = model1.getTotalCost();

                〡.nbox.ⅼ(Heading, () -> {
                    〡.nbox.ⅼ(Name, model1.name);
                    〡.nbox.ⅼ(TotalCost, total);
                    if (cost != total) {
                        〡.nbox.ⅼ(ItemViewStyle.Cost, "(", cost, ")");
                    }
                });

                // Status
                〡.nbox.ⅼ(null, VISIBLE, status -> {
                    double value = descriptor.get(status);

                    if (value != 0) {
                        〡.nbox.ⅼ(StatusValue, value, status.getUnit(), " ", status.name);
                    }
                });

                〡.nbox.ⅼ(DescriptionArea, () -> {
                    〡.nbox.ⅼ(null, descriptor.getAbilities(), ability -> {
                        AbilityDescriptor abilityDescriptor = ability.getDescriptor(Version.Latest);

                        〡.nbox.ⅼ(AbilityArea, () -> {
                            if (abilityDescriptor.isUnique()) {
                                〡.nbox.ⅼ(UniqueAbility, "UNIQUE");

                                // FORMAT
                            }

                            if (abilityDescriptor.isAura()) {
                                〡.nbox.ⅼ(UniqueAbility, "AURA");
                            }

                            〡.nbox.ⅼ(UniqueAbility, abilityDescriptor.isActive() ? "Active" : "Passive");

                            if (!ability.name.startsWith("#")) {
                                〡.nbox.ⅼ(UniqueAbility, "[", ability.name, "]");

                                // FORMAT
                            }

                            List token = abilityDescriptor.isActive() ? abilityDescriptor.getActive() : abilityDescriptor.getPassive();
                            〡.nbox.ⅼ(null, Widget.of(AbilityDescriptionView.class, ability, null, token));

                            // FORMAT
                        });
                    });
                });
            });
        });
    }

    /**
     * @version 2015/08/19 13:16:08
     */
    private static class AbilityDescriptionView extends DescriptionViewWidget<Ability> {

        /**
         * {@inheritDoc}
         */
        @Override
        protected int getLevel() {
            return calculator == null ? 0 : 1;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected Version getVersion() {
            return Version.Latest;
        }
    }
}
