/*
 * Copyright (C) 2016 Teemowork Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package teemowork.model;

import java.util.List;

import jsx.style.Style;
import jsx.style.StyleDSL;
import jsx.ui.StructureDSL;
import jsx.ui.Widget;
import teemowork.model.DescriptionView.Styles;
import teemowork.model.variable.Variable;
import teemowork.model.variable.VariableResolver;

/**
 * @version 2015/09/18 12:50:28
 */
public abstract class DescriptionView<D extends Describable> extends Widget<Styles> {

    /** The target descriptor to view. */
    protected final D describable;

    /** The calculator. */
    protected final StatusCalculator calculator;

    protected final List model3;

    /**
     * @param describable
     * @param calculator
     * @param model3
     */
    protected DescriptionView(D describable, StatusCalculator calculator, List model3) {
        this.describable = describable;
        this.calculator = calculator;
        this.model3 = model3;
    }

    /**
     * <p>
     * Specify the current level of descriptor.
     * </p>
     * 
     * @return
     */
    protected abstract int getLevel();

    /**
     * {@inheritDoc}
     */
    @Override
    protected void virtualize() {
        new StructureDSL() {
            {
                box($.Passive, contents(model3, text -> {
                    if (text instanceof Variable) {
                        writeVariable((Variable) text, getLevel());
                    } else {
                        text(text);
                    }
                }));
            }

            /**
             * <p>
             * </p>
             * 
             * @param root
             * @param variable
             * @param level
             */
            protected void writeVariable(Variable variable, int level) {
                VariableResolver resolver = variable.getResolver();
                Status status = variable.getStatus();
                List<Variable> amplifiers = variable.getAmplifiers();

                if (!resolver.isSkillLevelBased()) {
                    level = resolver.convertLevel(calculator);
                }

                // compute current value
                text($.ComputedValue, status.format(variable.calculate(Math.max(1, level), calculator)));

                // All values
                int size = resolver.estimateSize();
                int current = level;

                if (1 < size || !amplifiers.isEmpty()) {
                    text("(");

                    if (1 < size) {
                        box($.Variable, contents(1, size, i -> {
                            String description = resolver.getLevelDescription(i);

                            box($.Value, If(i == current, $.Current), If(description, title(description), $.Indicator), () -> {
                                text(round(resolver.compute(i), 2));
                            });
                        }));
                    }

                    writeAmplifier(amplifiers, level, calculator);
                    text(")");
                }
            }

            /**
             * <p>
             * Write skill amplifier.
             * </p>
             * 
             * @param root A element to write.
             * @param amplifiers A list of skill amplifiers.
             * @param level A current skill level.
             */
            public void writeAmplifier(List<Variable> amplifiers, int level, StatusCalculator calculator) {
                if (!amplifiers.isEmpty()) {
                    box($.Amplifiers, contents(amplifiers, amplifier -> {
                        box($.Amplifier, () -> {
                            int amp = level;

                            text("+", amplifier.getStatus());

                            VariableResolver resolver = amplifier.getResolver();

                            if (!resolver.isSkillLevelBased()) {
                                amp = resolver.convertLevel(calculator);
                            }

                            int estimated = resolver.estimateSize();
                            int size = estimated == 0 ? amplifier.getAmplifiers().isEmpty() ? 0 : 1 : estimated;
                            int current = amp;

                            box(contents(1, size, i -> {
                                String description = resolver.getLevelDescription(i);

                                box($.Value, If(size != 1 && i == current, $.Current), If(description, title(description), $.Indicator), () -> {
                                    text(round(amplifier.calculate(i, calculator, true), 4));
                                });
                            }));

                            text(amplifier.getStatus().name().endsWith("Ratio") ? "%" : "");
                            if (!amplifier.getAmplifiers().isEmpty()) {
                                text("(");
                                writeAmplifier(amplifier.getAmplifiers(), current, calculator);
                                text(")");
                            }
                        });
                    }));
                }
            }
        };
    }

    /**
     * <p>
     * Returns the closest {@code long} to the argument, with ties rounding up.
     * </p>
     * <p>
     * Special cases:
     * <ul>
     * <li>If the argument is NaN, the result is 0.</li>
     * <li>If the argument is negative infinity or any value less than or equal to the value of
     * {@code Long.MIN_VALUE}, the result is equal to the value of {@code Long.MIN_VALUE}.</li>
     * <li>If the argument is positive infinity or any value greater than or equal to the value of
     * {@code Long.MAX_VALUE}, the result is equal to the value of {@code Long.MAX_VALUE}.</li>
     * </ul>
     * 
     * @param value A floatingpoint value to be rounded.
     * @param precision
     * @return The value of the argument rounded to the nearest {@code int} value.
     */
    private static double round(double value, int precision) {
        double factor = Math.pow(10, precision);
        return Math.round(value * factor) / factor;
    }

    /**
     * @version 2015/08/20 15:59:24
     */
    static class Styles extends StyleDSL {

        Style ComputedValue = () -> {
        };

        Style Value = () -> {
            not(lastChild(), () -> {
                after(() -> {
                    content.text("/");
                    font.color(170, 170, 170);
                    margin.horizontal(1, px);
                });
            });
        };

        Style Current = () -> {
            font.color(rgba(160, 123, 1, 1));
        };

        Style Passive = () -> {
            display.block();
        };

        Style Indicator = () -> {
            cursor.help();
        };

        Style Variable = () -> {
            font.color(90, 90, 90);
        };

        Style Amplifiers = () -> {
            prev().with(Variable, () -> {
                margin.left(0.4, em);
            });
        };

        Style Amplifier = () -> {
            font.color(25, 111, 136);
            display.opacity(0.8);

            not(firstChild(), () -> {
                margin.left(0.4, em);
            });
        };
    }
}
