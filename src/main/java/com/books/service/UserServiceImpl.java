package com.books.service;

import com.books.domain.*;
import com.books.domain.security.PasswordResetToken;
import com.books.domain.security.UserRole;
import com.books.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
public class UserServiceImpl implements UserService{

    private static final Logger LOG = LoggerFactory.getLogger(UserService.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Autowired
    private UserPaymentRepository userPaymentRepositoryObj;

    @Autowired
    private UserShippingRepository userShippingRepositoryObj;

    @Override
    public PasswordResetToken getPasswordResetToken(final String token) {
        return passwordResetTokenRepository.findByToken(token);
    }

    @Override
    public void createPasswordResetTokenForUser(final User user, final String token) {
        final PasswordResetToken myToken = new PasswordResetToken(token, user);
        passwordResetTokenRepository.save(myToken);
    }


    @Override
    public User findByUsername(String username) {
        return userRepository.findByUsername(username);
    }


    @Override
    public User findByEmail (String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    public User createUser(User user, Set<UserRole> userRoles){
        User localUser = userRepository.findByUsername(user.getUsername());

        if(localUser != null) {
            LOG.info("user {} already exists. Nothing will be done.", user.getUsername());
        } else {
            for (UserRole ur : userRoles) {
                roleRepository.save(ur.getRole());
            }

            user.getUserRoles().addAll(userRoles);

            ShoppingCart shoppingCart = new ShoppingCart();
            shoppingCart.setUser(user);
            user.setShoppingCart(shoppingCart);

            user.setUserShippingList(new ArrayList<UserShipping>());
            user.setUserPaymentList(new ArrayList<UserPayment>());

            System.out.println("hasta aqui tiene todo correcto");

            localUser = userRepository.save(user);
        }

        return localUser;
    }




    @Override
    public User save(User user) {
        return userRepository.save(user);
    }





    @Override
    public void updateUserBilling(UserBilling userBilling, UserPayment userPayment, User user) {
        userPayment.setUser(user);
        userPayment.setUserBilling(userBilling);
        userPayment.setDefaultPayment(true);
        userBilling.setUserPayment(userPayment);
        user.getUserPaymentList().add(userPayment);
        save(user);
    }





    @Override
    public void setUserDefaultPayment(Long defaultPaymentId, User user) {
        List<UserPayment> userPaymentList = (List<UserPayment>) userPaymentRepositoryObj.findAll();

        for (UserPayment userPaymentObj : userPaymentList){
            if(userPaymentObj.getId().equals(defaultPaymentId)){
                userPaymentObj.setDefaultPayment(true);
                userPaymentRepositoryObj.save(userPaymentObj);
            }
            else{
                userPaymentObj.setDefaultPayment(false);
                userPaymentRepositoryObj.save(userPaymentObj);
            }
        }
    }




    @Override
    public void updateUserShipping(UserShipping userShipping, User user) {
        userShipping.setUser(user);
        userShipping.setUserShippingDefault(true);
        user.getUserShippingList().add(userShipping);
        save(user);
    }




    @Override
    public void setUserDefaultShipping(Long userShippingId, User user) {
        List<UserShipping> userShippingList = (List<UserShipping>) userShippingRepositoryObj.findAll();

        for (UserShipping userShippingObj : userShippingList){
            if(userShippingObj.getId().equals(userShippingId)){
                userShippingObj.setUserShippingDefault(true);
                userShippingRepositoryObj.save(userShippingObj);
            }
            else{
                userShippingObj.setUserShippingDefault(false);
                userShippingRepositoryObj.save(userShippingObj);
            }
        }
    }



}
