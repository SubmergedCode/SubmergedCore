/**
 * SubmergedCore 1.0
 * Copyright (C) 2014 CodingBadgers <plugins@mcbadgercraft.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package uk.submergedcode.SubmergedCore.error;

import java.lang.Thread.UncaughtExceptionHandler;

public class ExceptionHandler implements UncaughtExceptionHandler {

    private static final ExceptionHandler instance;

    static {
        instance = new ExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(instance);
    }

    public static boolean handleException(Throwable e) {
        e.printStackTrace();
        ReportExceptionRunnable run = new ReportExceptionRunnable(e);
        return run.run();
    }

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        handleException(e);
    }
}
