/*
 * -----------------------------------------------------------------------
 * Copyright © 2012 Meno Hochschild, <http://www.menodata.de/>
 * -----------------------------------------------------------------------
 * This file (TimeSource.java) is part of project Time4J.
 *
 * Time4J is free software: You can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Time4J is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Time4J. If not, see <http://www.gnu.org/licenses/>.
 * -----------------------------------------------------------------------
 */

package net.time4j.base;


/**
 * <p>Repr&auml;sentiert eine beliebige Uhr als Quelle der aktuellen
 * Weltzeit. </p>
 *
 * @param   <T> generic type of current time
 * @author  Meno Hochschild
 */
public interface TimeSource<T extends UnixTime> {

    //~ Methoden ----------------------------------------------------------

    /**
     * <p>Liefert die aktuelle Zeit. </p>
     *
     * @return  current time in seconds as {@code UnixTime} object
     */
    T currentTime();

}
