package ru.dmerkushov.mkstorage.loadtest;

import ru.dmerkushov.loadtest.LoadTesting;

public class MKSLoadTest {

    public static void main(String[] args) throws Exception {
        LoadTesting loadTesting = new LoadTesting(100, 10000, 600000);
        loadTesting.runLoadTest(MKSSimulation.class);
    }
}
