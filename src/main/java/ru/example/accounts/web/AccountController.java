package ru.example.accounts.web;

import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import ru.example.accounts.backend.dao.AccountRepository;
import ru.example.accounts.backend.dao.OperationRepository;
import ru.example.accounts.backend.model.*;
import ru.example.accounts.backend.security.JwtToken;
import ru.example.accounts.backend.service.JwtUserDetailsService;
import ru.example.accounts.backend.dao.UserRepository;
import ru.example.accounts.backend.service.MoneyOperations;
import ru.example.accounts.backend.service.UserService;

import javax.xml.bind.ValidationException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Account controller.
 */
@RestController
@RequestMapping("/accounts")
public class AccountController {

    private UserService service;
    private AccountRepository accountRepository;
    private AuthenticationManager authenticationManager;
    private UserRepository userRepository;
    private OperationRepository operationRepository;
    private JwtUserDetailsService jwtUserDetailsService;
    private JwtToken jwtToken;

    @Autowired
    public AccountController(UserService service, AuthenticationManager authenticationManager,
                             UserRepository userRepository, AccountRepository accountRepository,
                             OperationRepository operationRepository,
                             JwtUserDetailsService jwtUserDetailsService, JwtToken jwtToken) {
        this.service = service;
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.accountRepository = accountRepository;
        this.operationRepository = operationRepository;
        this.jwtUserDetailsService = jwtUserDetailsService;
        this.jwtToken = jwtToken;
    }




    @RequestMapping(value = "/signin", method = RequestMethod.POST)
    public ResponseEntity<?> createAuthenticationToken(@RequestBody JwtRequest authenticationRequest) throws Exception {
        authenticate(authenticationRequest.getUsername(), authenticationRequest.getPassword());
        final UserDetails userDetails = jwtUserDetailsService
                .loadUserByUsername(authenticationRequest.getUsername());
        final String token = jwtToken.generateToken(userDetails);
        return ResponseEntity.ok("Bearer " + token);
    }

    @PostMapping("/signup")
    public Boolean create(@RequestBody UserRequest userData) throws  ValidationException {
        if (userRepository.findByLogin(userData.getLogin()) != null) {
            throw new ValidationException("Username already existed");
        }
        String encodedPassword = new BCryptPasswordEncoder().encode(userData.getPassword());
        userRepository.save(new User(userData.getLogin(), encodedPassword, userData.getAddress(), userData.getPhone()  ));
        return true;
    }

    @PostMapping("/addacc")
    @ApiOperation("add account")
    public String addacc(@RequestParam String accCode) throws  ValidationException {
        String login = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByLogin(login);
        accountRepository.save(new Account(user, new BigDecimal("0"), accCode));
        return "Аккаунт создан";
    }

    @GetMapping("/getAccountList")
    @ApiOperation("get Accounts")
    public List<Account> getAccountList(@RequestParam String login) throws  ValidationException {
        User user = userRepository.findByLogin(login);
        return user.getAccountList();
    }

    @PostMapping("/refill")
    @ApiOperation("refill account")
    public String refill(@RequestParam String id, @RequestParam String currency, @RequestParam String amount) throws ValidationException {
        String login = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByLogin(login);

        if (user.getAccountList().size() == 0) {
            throw new ValidationException("У Вас не создано ни одного аккаунта!");
        }

        Account account = accountRepository.findById(id).orElse(null);
        switch (currency) {
            case "1":
                currency = "RUB";
                break;
            case "2":
                currency = "EUR";
                break;
            case "3":
                currency = "USD";
                break;
        }

        BigDecimal sum = new BigDecimal(amount);

        account.setAmount(account.getAmount().add(sum));
        accountRepository.save(account);
        return "Счёт пополнен";
    }

    @PostMapping("/sendmoney")
    @ApiOperation("send money to other user")
    public String sendmoney(@RequestParam String phone, @RequestParam String amount, String id) throws ValidationException {
        String login = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByLogin(login);
        if (user.getPhone().equals(phone)) {
            throw new ValidationException("Счёт списания и зачисления должны быть различны!");
        }


        User toUser = userRepository.findByPhone(phone); // получили другого пользователя

        if (toUser == null) {
            throw new ValidationException("Пользователь не найден!");
        }

        Account fromAcc = accountRepository.findById(id).orElse(null);
        BigDecimal sum = null;
        boolean fl = true;

        if ((fromAcc.getAmount().compareTo(new BigDecimal("0.00")) <= 0)) {
            throw new ValidationException("Недостаточно средств для перевода.");
        }

        sum = new BigDecimal(amount);
        if ((sum.compareTo(fromAcc.getAmount())) > 0) {
            throw new ValidationException("Вы ввели сумму, превышающую остаток средств.");
        }


        Account toAcc = null;

        List<Account> toUserAccounts = toUser.getAccountList();
        // проверяем, есть ли у другого пользователя аккаунт в нужной валюте
        if (toUserAccounts.size() != 0) {

            for (Account account : toUserAccounts)
                if (account.getAccCode().equals(fromAcc.getAccCode())) toAcc = account;
            if (toAcc == null) toAcc = toUserAccounts.get(toUserAccounts.size() - 1);
        }
        else {
            throw new ValidationException("Невозможно сделать перевод этому пользователю!\n");
        }

        BigDecimal oldBalanceFrom = fromAcc.getAmount();
        BigDecimal oldBalanceTo = toAcc.getAmount();

        MoneyOperations.Sub(fromAcc, sum, fromAcc.getAccCode());

        accountRepository.save(fromAcc);
        accountRepository.save(toAcc);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date date = Calendar.getInstance().getTime();


        operationRepository.save(new Operation(sdf.format(date), fromAcc.getAccCode(),
                fromAcc, toAcc, oldBalanceFrom, oldBalanceTo));

        return "Перевод выполнен";
    }


    @GetMapping("/history")
    @ApiOperation("history operations")
    public List<Operation> history() {
        String login = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByLogin(login);
        List<Operation> operationList = new ArrayList<>();

        for (Account account : user.getAccountList()){
            operationList.addAll(operationRepository.findByFromAcc(account));
        }
        return operationList;
    }


    private void authenticate(String username, String password) throws Exception {
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
        } catch (DisabledException e) {
            throw new Exception("USER_DISABLED", e);
        } catch (BadCredentialsException e) {
            throw new Exception("INVALID_CREDENTIALS", e);
        }

    }

}
