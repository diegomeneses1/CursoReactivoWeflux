package com.example.service;


import com.example.exception.BalanceNotEnoughException;
import com.example.exception.NotFoundException;
import com.example.exception.NotValidException;
import com.example.model.Cashout;
import com.example.model.User;
import com.example.repository.CashoutRepository;
import com.example.service.interfaces.ICashoutService;
import com.example.service.interfaces.IPaymentRestClient;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.function.Function;


@Service
public class CashoutService implements ICashoutService {

    private final CashoutRepository cashoutRepository;
    private final UserService userService;
    private final IPaymentRestClient restPayment;

    public CashoutService(CashoutRepository cashoutRepository, UserService userService, IPaymentRestClient restPayment) {
        this.cashoutRepository = cashoutRepository;
        this.userService = userService;
        this.restPayment = restPayment;
    }


    @Override
    public Mono<Cashout> save(Cashout cashout) {
        return userService.findById(cashout.getUserId())
                .switchIfEmpty(Mono.error(new NotFoundException("Usuario no encontrado")))
                .onErrorMap(throwable -> new NotValidException("Error Usuario no encontrado"))
                .doOnError(System.out::println)
                .flatMap(user -> validateBalance(user, cashout))
                .doOnNext(user -> System.out.println("Llamando al servicio de pagos con "+user.toString()))
                .flatMap(validatePayment())
                .doOnNext(System.out::println)
                .flatMap(res -> switch (res) {
                    case "El pago no puede continuar"
                            -> Mono.error(new NotValidException(res));
                    case "El pago puede ser procesado"  -> cashoutRepository.save(cashout);
                    default -> Mono.error(new NotValidException("Resultado no valido"));
                });
    }



    private Mono<User> validateBalance(User user, Cashout cashout) {
        return user.getBalance() >= cashout.getAmount()
                ? Mono.just(user)
                : Mono.error(new BalanceNotEnoughException("El balance no cubre los gastos"));
    }

    Function<User, Mono<String>> validatePayment() {
        return user -> restPayment.validatePayment(user.getId())
                .timeout(Duration.ofSeconds(3))
                .retryWhen(
                        Retry.backoff(3, Duration.ofSeconds(2))
                                .filter(error -> !(error instanceof NotValidException))
                ).doOnError(System.out::println);
    }

    private Mono<Void> updateUserBalance(User user, Cashout cashout) {
        user.setBalance(user.getBalance() - cashout.getAmount());
        return userService.updateBalance(user).then();
    }

    //@Override
    @Override
    public Flux<Cashout> findAll() {
        return cashoutRepository.findAll();
    }

    //@Override
    @Override
    public Mono<Cashout> findById(String id) {
        return cashoutRepository.findById(id);
    }

    @Override
    public Flux<Cashout> findByUserid(String userId) {
        return cashoutRepository.findByuserId(userId);
    }


}

