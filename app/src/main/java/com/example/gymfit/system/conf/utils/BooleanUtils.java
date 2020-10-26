package com.example.gymfit.system.conf.utils;

import java.util.List;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public final class BooleanUtils {
    private BooleanUtils() {}

    // convert a List into array
    public static boolean[] listToArray(List<Boolean> list) {
        int length = list.size();
        boolean[] arr = new boolean[length];
        for (int i = 0; i < length; i++)
            arr[i] = list.get(i);
        return arr;
    }

    // Given a Stream<Boolean> you will be use this static var into collect method
    public static final Collector<Boolean, ?, boolean[]> TO_BOOLEAN_ARRAY = Collectors.collectingAndThen(Collectors.toList(), BooleanUtils::listToArray);
}
