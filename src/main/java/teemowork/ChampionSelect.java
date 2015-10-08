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

import static jsx.ui.StructureDescriptor.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SetProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import js.dom.UIAction;
import jsx.style.StyleDescriptor;
import jsx.style.ValueStyle;
import jsx.style.property.Background.BackgroundImage;
import jsx.style.value.Color;
import jsx.style.value.Numeric;
import jsx.ui.Model;
import jsx.ui.Style;
import jsx.ui.Widget;
import jsx.ui.Widget1;
import jsx.ui.piece.CheckBox;
import jsx.ui.piece.Input;
import jsx.ui.piece.UI;
import kiss.Events;
import kiss.I;
import teemowork.model.Champion;
import teemowork.model.Skill;
import teemowork.model.SkillDescriptor;
import teemowork.model.Status;
import teemowork.model.Version;
import teemowork.model.variable.Variable;

/**
 * @version 2015/10/07 2:56:10
 */
public class ChampionSelect extends Widget {

    private final Teemowork application = I.make(Teemowork.class);

    /** The skill filters. */
    private final SkillFiltersWidget[] groups = {
            new SkillFiltersWidget("ダメージ",
                    type(Status.PhysicalDamage),
                    type(Status.MagicDamage),
                    type(Status.TrueDamage),
                    type("範囲攻撃", Status.Radius)),
            new SkillFiltersWidget("参照",
                    referSelf(Status.Health, Status.HealthRatio, Status.BounusHealth, Status.BaseHealth),
                    referSelf(Status.Mana, Status.ManaRatio, Status.BounusMana),
                    referSelf(Status.AD, Status.ADRatio, Status.BounusAD, Status.BaseAD),
                    referSelf(Status.AP, Status.APRatio),
                    referSelf(Status.AR, Status.ARRatio, Status.BounusAR),
                    referSelf(Status.MR, Status.MRRatio, Status.BounusMR),
                    referSelf(Status.MS, Status.MSRatio, Status.BounusMS),
                    referSelf(Status.CurrentHealthRatio),
                    referSelf(Status.CurrentManaRatio),
                    referSelf(Status.MissingHealthRatio),
                    referSelf(Status.MissingManaRatio),
                    referSelf(Status.MissingHealthPercentage),
                    referSelf(Status.MissingManaPercentage),
                    addReferEnemy(Status.TargetMaxHealthRatio),
                    addReferEnemy(Status.TargetCurrentHealthRatio),
                    addReferEnemy(Status.TargetBounusHealthRatio),
                    addReferEnemy(Status.TargetMissingHealthRatio),
                    addReferEnemy(Status.TargetAP)),
            new SkillFiltersWidget("Buff",
                    type(Status.Health, Status.HealthRatio),
                    type(Status.Mana, Status.ManaRatio),
                    type(Status.AD, Status.ADRatio),
                    type(Status.AP, Status.APRatio),
                    type(Status.AR, Status.ARRatio),
                    type(Status.MR, Status.MRRatio),
                    type(Status.MS, Status.MSRatio),
                    type(Status.AS, Status.ASRatio),
                    type(Status.CDR, Status.CDRRatio),
                    type(Status.LS, Status.LSRatio),
                    type(Status.SV, Status.SVRatio),
                    type(Status.ARPen, Status.ARPenRatio),
                    type(Status.MRPen, Status.MRPenRatio),
                    type(Status.Shield, Status.PhysicalShield, Status.MagicShield),
                    type("スキル無効", Status.SpellShield),
                    type(Status.IgnoreCC, Status.RemoveCC),
                    type(Status.IgnoreSlow),
                    type(Status.IgnoreUnitCollision)),
            new SkillFiltersWidget("Debuff",
                    type(Status.MSSlow, Status.MSSlowRatio, Status.Slow, Status.SlowRatio),
                    type(Status.ASSlow, Status.ASSlowRatio, Status.Slow, Status.SlowRatio),
                    type(Status.Stun),
                    type(Status.Snare),
                    type(Status.Taunt),
                    type(Status.Knockback),
                    type(Status.Knockup),
                    type(Status.Charm),
                    type(Status.Fear),
                    type(Status.Terrified),
                    type(Status.Suppression),
                    type(Status.Suspension),
                    type(Status.Silence),
                    type(Status.Blind)),
            new SkillFiltersWidget("回復",
                    type(Status.RestoreHealth, Status.RestoreHealthRatio, Status.Hreg, Status.HregPerLv, Status.HregRatio),
                    type(Status.RestoreMana, Status.Mreg, Status.MregPerLv, Status.MregRatio),
                    type(Status.RestoreEnergy, Status.EnergyPerLv, Status.EnergyRatio)),
            new SkillFiltersWidget("その他",
                    type("AAタイマー解消", Status.ResetAATimer),
                    type("オンヒット効果", Status.OnHitEffect),
                    type("CD解消", Status.CDDecrease, Status.CDDecreaseRatio, Status.CD),
                    type(Status.Visionable))};

    private final Input input = UI.input().placeholder("Champion Name").style($.SearchByName);

    public final Events<Champion> selectChampion = when(UIAction.Click).at($.Container, Champion.class);

    @Model
    private final SetProperty<Predicate<Champion>> selectedFilters = I.make(SetProperty.class);

    @Model
    private final BooleanProperty showSkillFilters = new SimpleBooleanProperty();

    /**
     * 
     */
    public ChampionSelect() {
        selectChampion.to(champion -> application.champion(champion));

        when(UIAction.Click).at($.FilterDetail).toggle().to(showSkillFilters::set);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void virtualize() {
        box($.Root, () -> {
            box($.Filters, input, () -> {
                text($.FilterDetail, "スキルで絞込");
                box($.SkillFilters, If(showSkillFilters, $.ShowDetailFilter), contents(groups));
            });
            box($.ImageSet, contents(Champion.getAll(), champion -> {
                box($.Container, If(!filter(champion) || !champion.match(input.value.get()), $.Unselected), () -> {
                    box($.IconImage, $.IconPosition.of(champion));
                    text($.Title, champion.name);
                });
            }));
        });
    }

    /**
     * <p>
     * Filter by conditions.
     * </p>
     * 
     * @param champion
     * @return
     */
    private boolean filter(Champion champion) {
        if (selectedFilters.isEmpty()) {
            return true;
        }

        for (Predicate<Champion> filter : selectedFilters) {
            if (!filter.test(champion)) {
                return false;
            }
        }
        return true;
    }

    /**
     * <p>
     * Add filter.
     * </p>
     * 
     * @param statuses
     * @return
     */
    private static SkillFilter type(Status... statuses) {
        return type(statuses[0].name, statuses);
    }

    /**
     * <p>
     * Add filter.
     * </p>
     * 
     * @param statuses
     * @return
     */
    private static SkillFilter type(String name, Status... statuses) {
        return new SkillFilter(name, champion -> {
            Info info = Info.of(champion);

            for (Status status : statuses) {
                if (info.types.contains(status)) {
                    return true;
                }
            }
            return false;
        });
    }

    /**
     * <p>
     * Add filter.
     * </p>
     * 
     * @param statuses
     * @return
     */
    private static SkillFilter referSelf(Status... statuses) {
        return referSelf(statuses[0].name, statuses);
    }

    /**
     * <p>
     * Add filter.
     * </p>
     * 
     * @param statuses
     * @return
     */
    private static SkillFilter referSelf(String name, Status... statuses) {
        return new SkillFilter("自身の" + name, champion -> {
            Info info = Info.of(champion);

            for (Status status : statuses) {
                if (info.amplifiers.contains(status)) {
                    return true;
                }
            }
            return false;
        });
    }

    /**
     * <p>
     * Add filter.
     * </p>
     * 
     * @param statuses
     * @return
     */
    private static SkillFilter addReferEnemy(Status... statuses) {
        return addReferEnemy(statuses[0].name, statuses);
    }

    /**
     * <p>
     * Add filter.
     * </p>
     * 
     * @param statuses
     * @return
     */
    private static SkillFilter addReferEnemy(String name, Status... statuses) {
        return new SkillFilter(name, champion -> {
            Info info = Info.of(champion);

            for (Status status : statuses) {
                if (info.amplifiers.contains(status)) {
                    return true;
                }
            }
            return false;
        });
    }

    /**
     * @version 2015/10/05 14:12:22
     */
    private static class SkillFiltersWidget extends Widget {

        /** A filter group name. */
        private final String name;

        /** The filter widgets. */
        private final SkillFilterWidget[] filters;

        /**
         * @param name
         * @param filters
         */
        private SkillFiltersWidget(String name, SkillFilter... filters) {
            this.name = name;
            this.filters = Widget.of(SkillFilterWidget.class, filters);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected void virtualize() {
            box($.Group, () -> {
                text($.Name, name);

                box($.Items, contents(filters));
            });
        }

        /**
         * @version 2015/10/05 14:30:24
         */
        private static class $ extends StyleDescriptor {

            private static Style Group = () -> {
                display.block();
                margin.bottom(0.8, em);
            };

            private static Style Name = () -> {
                display.block();
                font.weight.bold();
                margin.bottom(0.4, em);
            };

            private static Style Items = () -> {
                display.flex().wrap.enable();
            };
        }
    }

    /**
     * @version 2015/10/05 11:21:14
     */
    private class SkillFilterWidget extends Widget1<SkillFilter> {

        /** The chech box. */
        private CheckBox check = UI.checkbox(model1.use, model1.name).style($.Filter).change(on -> {
            if (on) {
                selectedFilters.add(model1.filter);
            } else {
                selectedFilters.remove(model1.filter);
            }
        });

        /**
         * {@inheritDoc}
         */
        @Override
        protected void virtualize() {
            widget(check);
        }

    }

    /**
     * @version 2015/10/07 1:37:07
     */
    private static class SkillFilter {

        /** The filter manager. */
        private static final List<SkillFilter> filters = new ArrayList();

        /** The filter name. */
        private final StringProperty name;

        /** The actual filter. */
        private final Predicate<Champion> filter;

        /** The filter usage. */
        private final BooleanProperty use = new SimpleBooleanProperty();

        /**
         * @param name
         * @param filter
         */
        private SkillFilter(String name, Predicate<Champion> filter) {
            this.name = new SimpleStringProperty(name);
            this.filter = filter;

            filters.add(this);
        }
    }

    /**
     * @version 2015/10/07 0:04:51
     */
    private static class Info {

        /** The cache. */
        private static final Map<Champion, Info> infos = new HashMap();

        /** The status index. */
        private final Set<Status> types = new HashSet();

        /** The status index. */
        private final Set<Status> amplifiers = new HashSet();

        /**
         * @param champion
         */
        private Info(Champion champion) {
            for (Skill skill : champion.skills) {
                SkillDescriptor descriptor = skill.getDescriptor(Version.Latest);

                parse(descriptor.getPassive());
                parse(descriptor.getActive());
            }
        }

        /**
         * <p>
         * Helper method to parse status.
         * </p>
         * 
         * @param tokens
         */
        private void parse(List tokens) {
            for (Object token : tokens) {
                if (token instanceof Variable) {
                    Variable variable = (Variable) token;
                    Status status = variable.getStatus();
                    types.add(status);

                    List<Variable> amplifiers = variable.getAmplifiers();

                    for (Variable amplifier : amplifiers) {
                        this.amplifiers.add(amplifier.getStatus());
                    }
                }
            }
        }

        /**
         * <p>
         * Retrieve {@link Info} for the specified champion.
         * </p>
         * 
         * @param champion
         * @return
         */
        private static Info of(Champion champion) {
            return infos.computeIfAbsent(champion, Info::new);
        }
    }

    /**
     * @version 2015/01/30 14:32:48
     */
    static class $ extends StyleDescriptor {

        private static final Color backColor = new Color(0, 10, 10);

        private static final Numeric ImageSize = new Numeric(70, px);

        private static final Numeric ImagesSize = ImageSize.multiply(10);

        private static Style Root = () -> {
            display.block();
            margin.auto();
            box.width(ImagesSize.add(2));
        };

        private static Style ImageSet = () -> {
            display.inlineFlex().direction.row().wrap.enable();
            border.top.solid().width(2, px).color(backColor);
            border.left.solid().width(2, px).color(backColor);
        };

        private static Style Container = () -> {
            position.relative();
            display.block();
        };

        private static Style Unselected = () -> {
            display.none();
        };

        private static Style IconImage = () -> {
            display.block();
            box.size(ImageSize);
            border.bottom.solid().width(2, px).color(backColor);
            border.right.solid().width(2, px).color(backColor);
            cursor.pointer();
            background.image("src/main/resources/teemowork/champions.jpg").cover().borderBox();

            after(() -> {
                content.text("");
                display.block();
                position.absolute();
                box.width(100, percent).height(100, percent).opacity(1);
                background.color(hsla(0, 100, 100, 0.2));

                transit().duration(0.2, s).easeInOut().whenHover(() -> {
                    box.opacity(0);
                });
            });
        };

        private static ValueStyle<Champion> IconPosition = chmapion -> {
            background.horizontal(chmapion.getIconPosition());
        };

        private static Style Title = () -> {
            Numeric boxWidth = ImageSize.add(40);
            Color color = new Color(0, 98, 97, 1);

            font.weight.bold().size(18, px);
            text.align.center();
            line.height(20, px);
            padding.size(5, px);
            background.image(BackgroundImage.of(linear(color.opacify(-0.4), color)));
            position.absolute().left(50, percent).bottom(ImageSize.add(20));
            margin.left(boxWidth.divide(-2));
            box.minWidth(boxWidth).zIndex(1).opacity(0);
            border.width(4, px).solid().color(color.lighten(-100)).radius(5, px);
            pointerEvents.none();

            // createBottomBubble(7);
            createBottomBubble(7, new Numeric(4, px), color.lighten(-100), color);

            transit().duration(0.2, s).delay(100, ms).easeInOut().whenSiblingHover(() -> {
                box.opacity(1);
                position.bottom(ImageSize);
            });
        };

        private static Style SearchByName = () -> {

        };

        private static Style Filters = () -> {
            display.block();
            margin.bottom(10, px);
        };

        private static Style ShowDetailFilter = () -> {
            display.block();
        };

        private static Style SkillFilters = () -> {
            margin.top(1, em);
            font.size.smaller();

            display.none();
            box.height(0, px);

            transit().duration(0.5, s).easeInOut().when(ShowDetailFilter, () -> {
                display.block();
                box.height(100, percent);
            });
        };

        private static Style FilterDetail = () -> {
            font.size.small().color(Color.rgb(100, 100, 100));
            text.verticalAlign.bottom();
            margin.left(1, em);
            cursor.pointer();
        };

        private static Style Filter = () -> {
            box.width(ChampionSelect.$.ImagesSize.divide(5));
        };
    }
}
