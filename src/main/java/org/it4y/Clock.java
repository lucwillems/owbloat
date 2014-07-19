
/*
 * GNU GENERAL PUBLIC LICENSE
 * Copyright 2014 Luc Willems (T.M.M.)
 *
 * Version 2, June 1991
 *
 * Everyone is permitted to copy and distribute verbatim copies
 * of this license document, but changing it is not allowed.
 *
 *   http://www.gnu.org/licenses/gpl-2.0.html
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package org.it4y;

/**
 * Created by luc on 6/21/14.
 */
public class Clock {
    public static Clock DEFAULT=new Clock();

    Clock() {}

    public long get24BitCurrentTimesMillis() {
        return System.currentTimeMillis() & 0xffffffL;
    }

    public long get24BitnanoTime() {
        return System.nanoTime() & 0xffffffL;
    }

}
