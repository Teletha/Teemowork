/*
 * Copyright (C) 2013 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package teemowork;

import jsx.style.StyleDescriptor;
import jsx.style.value.Font;
import jsx.ui.StructureDescriptor.Style;

/**
 * @version 2013/03/24 16:34:23
 */
public class TeemoworkTheme extends StyleDescriptor {

    /** The main font. */
    public static final Font Main = new Font("http://fonts.googleapis.com/css?family=Source+Sans+Pro:400,600");

    /** The header font. */
    public static final Font Header = Main;

    /** The title/heading font. */
    public static final Font Title = new Font("http://fonts.googleapis.com/css?family=Yanone+Kaffeesatz");

    static Style HTML = () -> {
        font.family(Main.name).sansSerif();
        padding.horizontal(10, percent).top(10, px);
    };

    static Style Content = () -> {
        padding.top(20, px);
    };
}
