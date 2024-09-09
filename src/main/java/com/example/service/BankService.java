package com.example.service;

import com.example.exception.BusinessException;
import com.example.model.*;
import org.reactivestreams.Publisher;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import java.util.Arrays;
import java.util.List;

import static com.example.model.UpdateAccountRequest.*;

@Service
public class BankService {

    private final Sinks.Many<String> sink = Sinks.many().multicast().onBackpressureBuffer();

    public Flux<Transaction> llenarTransacciones(){
        //Creo un flux de transacciones
        Flux<Transaction> transactions = Flux.just(
                new Transaction("1", "1", 100.0),
                new Transaction("2", "1", 200.0),
                new Transaction("3", "2", 300.0)
        );
        return  transactions;
    }

    public Flux<Loan> llenarPrestamos(){
        //Creo un flux de transacciones
        Flux<Loan> loans = Flux.just(
                        new Loan("loan1", 5000.00, 0.05, "123"),
                        new Loan("loan2", 10000.00, 0.04, "123"),
                        new Loan("loan1", 5000.00, 0.05, "123"),
                        new Loan("loan2", 10000.00, 0.04, "123"),
                        new Loan("loan1", 5000.00, 0.05, "123"),
                        new Loan("loan2", 10000.00, 0.04, "123")
                );
        return  loans;
    }

    public Mono<Double> getBalance(String accountId) {
        // Caso de uso: Consultar el saldo actual de una cuenta bancaria. Sino hay balance se debe tener un valor de 0.0
        Flux<Transaction> transactions = llenarTransacciones();
        Flux<Transaction> filteredTransactions = transactions.filter(tx -> tx.getAccountId().equals(accountId));
        return filteredTransactions
                .map(Transaction::getAmount)      // Mapeamos las transacciones para obtener solo los montos
                .reduce(0.0, Double::sum)         // Sumamos todos los montos, comenzando desde 0.0
                .defaultIfEmpty(0.0);
    }

    public Mono<String> transferMoney(TransferRequest request) {
        // Caso de uso: Transferir dinero de una cuenta a otra. Hacer llamado de otro flujo simulando el llamado
        Flux<Transaction> transactions = llenarTransacciones();

        Flux<Transaction> updatedTransactions = transactions
                .flatMap(tx -> {
                    if (tx.getAccountId().equals(request.getFromAccount())) {
                        // Restar el monto de la cuenta de origen
                        double newAmount = tx.getAmount() - request.getAmount();
                        if (newAmount < 0) {
                            return Mono.error(new IllegalArgumentException("Saldo insuficiente en la cuenta de origen"));
                        }
                        return Mono.just(new Transaction(tx.getTransactionId(), tx.getAccountId(), newAmount));
                    } else if (tx.getAccountId().equals(request.getToAccount())) {
                        // Sumar el monto a la cuenta de destino
                        double newAmount = tx.getAmount() + request.getAmount();
                        return Mono.just(new Transaction(tx.getTransactionId(), tx.getAccountId(), newAmount));
                    } else {
                        // Si no es ni la cuenta de origen ni la de destino, devolver la transacción original
                        return Mono.just(tx);
                    }
                });

        return Mono.empty(); // Implementar la lógica de consulta aquí  ???????
    }

    public Flux<Transaction> getTransactions(String accountId) {
        // Caso de uso: Consultar el historial de transacciones de una cuenta bancaria.
        List<Transaction> transactions = Arrays.asList(
                new Transaction("1", accountId, 200.00),
                new Transaction("2", accountId, -150.00),
                new Transaction("3", accountId, 300.00)
        );

        return Flux.fromIterable(transactions).filter(tx -> tx.getAccountId().equals(accountId));
    }

    public Flux<Transaction> createAccount(CreateAccountRequest request) {
        // Caso de uso: Crear una nueva cuenta bancaria con un saldo inicial.
        Flux<Transaction> transactions = llenarTransacciones();
        Mono<Transaction> newAccount = Mono.just(new Transaction("5", request.getAccountId(), request.getInitialBalance()));
        Flux<Transaction> insertAccounts = Flux.concat(transactions, newAccount);
        return insertAccounts;
    }

    public Flux<Transaction> closeAccount(String accountId) {
        // Caso de uso: Cerrar una cuenta bancaria especificada. Verificar que la cuenta exista y si no existe debe retornar un error controlado
        Flux<Transaction> transactions = llenarTransacciones();
        Flux<Transaction> closeAccounts = transactions.filter(tx -> !tx.getAccountId().equals(accountId))
                .switchIfEmpty(new BusinessException("La cuenta no existe"));
        return closeAccounts;
    }

    public Flux<Transaction> updateAccount(UpdateAccountRequest request) {
        // Caso de uso: Actualizar la información de una cuenta bancaria especificada. Verificar que la ceunta exista y si no existe debe retornar un error controlado
        Flux<Transaction> transactions = llenarTransacciones();

        Flux<Transaction> updateAccounts = transactions.map(tx ->{
            if(tx.getAccountId().equals(request.getAccountId())){
                return new Transaction(tx.getTransactionId(), request.getNewData(), tx.getAmount());
            }
            return tx;
        }).switchIfEmpty(new BusinessException("La cuenta no existe"));

        return updateAccounts;
    }

    public Mono<CustomerProfile> getCustomerProfile(String accountId) {
        // Caso de uso: Consultar el perfil del cliente que posee la cuenta bancaria. Obtener los valores por cada uno de los flujos y si no existe alguno debe presentar un error
        Mono<String> customerIdMono = Mono.just("12345");
        Mono<String> nameMono = Mono.just("John Doe");
        Mono<String> emailMono = Mono.just("john.doe@example.com");

        // Combinar los monos para formar el perfil del cliente
        return Mono.zip(customerIdMono, nameMono, emailMono)
                .map(atributos -> {
                    String customerId = atributos.getT1();
                    String name = atributos.getT2();
                    String email = atributos.getT3();
                    return new CustomerProfile(customerId, name, email);
                })
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Error: No se encontraron datos para el ID: " + accountId)));
    }

    public Flux<Loan> getActiveLoans(String customerId) {
        // Caso de uso: Consultar todos los préstamos activos asociados al cliente especificado.
        List<Loan> loans = Arrays.asList(
                new Loan("loan1", 5000.00, 0.05, "123"),
                new Loan("loan2", 10000.00, 0.04, "123"),
                new Loan("loan1", 5000.00, 0.05, "123"),
                new Loan("loan2", 10000.00, 0.04, "123"),
                new Loan("loan1", 5000.00, 0.05, "123"),
                new Loan("loan2", 10000.00, 0.04, "123")
        );
        return Flux.fromIterable(loans).filter(tx -> tx.getCustomerid().equals(customerId))
                .switchIfEmpty(Flux.error(new IllegalArgumentException("No se econtraron prestamos para el customerId: " + customerId)));
    }



    public Flux<Double> simulateInterest(String accountId) {
        // Caso de uso: Simular el interés compuesto en una cuenta bancaria. Sacar un rago de 10 años y aplicar la siguiente formula = principal * Math.pow(1 + rate, year)
        double principal = 1000.00;
        double rate = 0.05;

        return Flux.range(1, 10)  // Rango de años (del 1 al 10)
                .map(year -> principal * Math.pow(1 + rate, year))  // Aplicar la fórmula del interés compuesto
                .doOnNext(interest -> System.out.println("Año: " + interest));  // Opción para imprimir los valores si lo deseas

    }

    public Mono<String> getLoanStatus(String loanId) {
        // Caso de uso: Consultar el estado de un préstamo. se debe tener un flujo balanceMono y interestRateMono. Imprimir con el formato siguiente el resultado   "Loan ID: %s, Balance: %.2f, Interest Rate: %.2f%%"
        //Obtengo el monto del prestamo
        Flux<Loan> loan = llenarPrestamos();
        Mono<Double> monoBalance = loan
                .filter(tx -> tx.getLoanId().equals(loanId))
                .next()  // Convierte el Flux en Mono<Loan>
                .map(Loan::getBalance)  // Mapear al atributo de tipo Double (por ejemplo, balance)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("No se econtraron prestamos para el loan ID: " + loanId)));

        Mono<Double> interestRateMono = Mono.just(3.5);  // Tasa de interés del 3.5%

        // Combinar los monos y formar el mensaje con el formato deseado
        return Mono.zip(monoBalance, interestRateMono)
                .map(tuple -> {
                    Double balance = tuple.getT1();
                    Double interestRate = tuple.getT2();
                    return String.format("Loan ID: %s, Balance: %.2f, Tasa de Interes: %.2f%%", loanId, balance, interestRate);
                })
                .doOnNext(status -> System.out.println(status));  // Opción para imprimir el resultado
    }


}
