package ru.iu3.fclient;

interface TransactionEvents {
    //кол-во попыток и сумма транзакции
    String enterPin(int ptc, String amount);

    //спользуется для получения результата транзакции
    void transactionResult(boolean result);
}

