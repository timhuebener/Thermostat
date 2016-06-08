package thermocompany.thermostat;

/**
 * Created by s152582 on 8-6-2016.
 */

import util.*;
public class Memory {
    static WeekProgram savedProgram;

    static void storeWeekProgram(WeekProgram savedProgramRec) {
        savedProgram = savedProgramRec;
    }

    static WeekProgram getWeekProgram() {
        return savedProgram;
    }

}
