/*
 * Copyright (C) 2016 Teemowork Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package teemowork;

import java.util.Locale;

import jsx.style.Style;
import jsx.style.StyleDSL;
import jsx.ui.StructureDSL;
import jsx.ui.Widget;
import jsx.ui.piece.Button;
import jsx.ui.piece.Input;
import jsx.ui.piece.Select;
import jsx.ui.piece.UI;
import kiss.I;
import kiss.Variable;
import teemowork.Setting.Styles;
import teemowork.api.GameAPI;
import teemowork.api.GameAPI.RiotMatchHistory;
import teemowork.model.Region;

/**
 * @version 2016/10/22 10:41:08
 */
public class Setting extends Widget<Styles> {

    /** The user preference. */
    private final UserPreference preference = I.make(UserPreference.class);

    /** The configuration item. */
    private final Select<Region> summerRegion = UI.select(Region.class, preference.region);

    /** The configuration item. */
    private final Input summerName = UI.input(preference.name).invalidIf(String::isEmpty, "サモナーネームを入力して下さい。");

    /** The configuration item. */
    private final Button update = UI.button().label("次回更新可能時刻").disableIf(summerName.invalid).click(() -> {
        GameAPI.user().flatMap(GameAPI::matchList).flatIterable(RiotMatchHistory::matches).flatMap(GameAPI::match).to(t -> {
            System.out.println(t);
        });
    });

    /** The configuration item. */
    private final Select<Locale> localeChampion = createSelectUI(preference.localeChampion);

    /** The configuration item. */
    private final Select<Locale> localeSkill = createSelectUI(preference.localeSkill);

    /** The configuration item. */
    private final Select<Locale> localeItem = createSelectUI(preference.localeItem);

    /** The configuration item. */
    private final Select<Locale> localeStatus = createSelectUI(preference.localeStatus);

    /**
     * <p>
     * Build select UI.
     * </p>
     */
    private Select<Locale> createSelectUI(Variable<Locale> variable) {
        return UI.select(UserPreference.AvailableLocales, variable).label(value -> value == Locale.ENGLISH ? "英語" : "日本語");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void virtualize() {
        new StructureDSL() {
            {
                text($.CategoryName, "サモナー");
                box($.Item, () -> {
                    text($.ItemName, "地域");
                    box(summerRegion);
                });
                box($.Item, () -> {
                    text($.ItemName, "名前");
                    box(summerName);
                });
                box($.Item, () -> {
                    text($.ItemName, "更新");
                    box(update);
                });

                text($.CategoryName, "表記言語");
                box($.Item, () -> {
                    text($.ItemName, "チャンピオン名");
                    box(localeChampion);;
                });
                box($.Item, () -> {
                    text($.ItemName, "スキル名");
                    box(localeSkill);
                });
                box($.Item, () -> {
                    text($.ItemName, "アイテム名");
                    box(localeItem);
                });
                box($.Item, () -> {
                    text($.ItemName, "ステータス名");
                    box(localeStatus);
                });
            }
        };
    }

    /**
     * @version 2015/10/19 18:51:45
     */
    static class Styles extends StyleDSL {

        Style CategoryName = () -> {
            display.block();
            font.weight.bold();
            margin.bottom(0.5, em);

            not(firstChild(), () -> {
                margin.top(1, em);
            });
        };

        Style Item = () -> {
            display.flex();
            font.size.small();
            margin.top(0.5, em);
        };

        Style ItemName = () -> {
            display.width(10, em).flex().alignItems.center();
        };
    }
}
