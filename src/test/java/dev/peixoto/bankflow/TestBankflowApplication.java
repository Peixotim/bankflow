package dev.peixoto.bankflow;

import org.springframework.boot.SpringApplication;

public class TestBankflowApplication {

    public static void main(String[] args) {
        SpringApplication.from(BankflowApplication::main).with(TestcontainersConfiguration.class).run(args);
    }

}
