package com.books.controller;

import com.books.domain.*;
import com.books.service.*;
import com.books.utility.MailConstructor;
import com.books.utility.USConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

@Controller
public class CheckOutController {

    private ShippingAddress shippingAddressObj = new ShippingAddress();
    private BillingAddress billingAddressObj = new BillingAddress();
    private Payment paymentObj = new Payment();

    @Autowired
    private UserService userServiceObj;

    @Autowired
    private CartItemService cartItemServiceObj;

    @Autowired
    private ShippingAddressService shippingAddressServiceObj;

    @Autowired
    private PaymentService paymentServiceObj;

    @Autowired
    private BillingAddressService billingAddressServiceObj;

    @Autowired
    private UserShippingService userShippingServiceObj;

    @Autowired
    private UserPaymentService userPaymentServiceObj;

    @Autowired
    private OrderService orderServiceObj;

    @Autowired
    private ShoppingCartService shoppingCartServiceObj;

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private MailConstructor mailConstructor;



    @GetMapping("/checkout")
    public String checkOut(@RequestParam("id") Long shoppingCartId, @RequestParam(value = "missingRequiredField",
            required = false) boolean missingRequiredField, Model model, Principal principal) {

        System.out.println("el valor de missinrequiredfiles es: " + missingRequiredField);

        User user = userServiceObj.findByUsername(principal.getName());

        if (!shoppingCartId.equals(user.getShoppingCart().getId())) {
            return "badRequestPage";
        }

        List<CartItem> cartItemList = cartItemServiceObj.findByShoppingCart(user.getShoppingCart());

        if (cartItemList.size() == 0) {
            model.addAttribute("emptyCart", true);
            return "redirect:/shoppingCart/cart";
        }

        for (CartItem cartItem : cartItemList) {
            if (cartItem.getBook().getInStockNumber() < cartItem.getQty()) {
                model.addAttribute("notEnoughStock", true);
                return "redirect:/shoppingCart/cart";
            }
        }

        List<UserShipping> userShippingList = user.getUserShippingList();
        List<UserPayment> userPaymentList = user.getUserPaymentList();

        model.addAttribute("userShippingList", userShippingList);
        model.addAttribute("userPaymentList", userPaymentList);

        //se envia el true o false dependiend de la situacion.
        model.addAttribute("emptyPaymentList", userPaymentList.size() == 0);
        model.addAttribute("emptyShippingList", userShippingList.size() == 0);


        //MODIFICAN VALORES CON SET POR DEFECTO PARA SHIPPING Y PAYMENT USER
        ShoppingCart shoppingCart = user.getShoppingCart();
        for (UserShipping userShippingObj : userShippingList) {
            if (userShippingObj.isUserShippingDefault()) {
                shippingAddressServiceObj.setByUserShipping(userShippingObj, shippingAddressObj);
            }
        }

        for (UserPayment userPaymentObj : userPaymentList) {
            if (userPaymentObj.isDefaultPayment()) {
                paymentServiceObj.setByUserPayment(userPaymentObj, paymentObj);
                billingAddressServiceObj.setByUserBilling(userPaymentObj.getUserBilling(), billingAddressObj);
            }
        }

        model.addAttribute("shippingAddress", shippingAddressObj);
        model.addAttribute("payment", paymentObj);
        model.addAttribute("billingAddress", billingAddressObj);
        model.addAttribute("cartItemList", cartItemList);
        model.addAttribute("shoppingCart", user.getShoppingCart());

        List<String> stateList = USConstants.listOfUSStatesCode;
        Collections.sort(stateList);
        model.addAttribute("stateList", stateList);

        model.addAttribute("classActiveShipping", true);

        if (missingRequiredField) {
            model.addAttribute("missinRequiredField", true);
        }

        return "checkout";
    }




    @GetMapping("/setShippingAddress")
    public String setShippingAddress(@RequestParam("userShippingId") Long userShippingId,
                                     Principal principal, Model model) {

        User user = userServiceObj.findByUsername(principal.getName());
        UserShipping userShipping = userShippingServiceObj.findById(userShippingId);

        if (!userShipping.getUser().getId().equals(user.getId())) {
            return "badRequestPage";
        } else {
            shippingAddressServiceObj.setByUserShipping(userShipping, shippingAddressObj);
            List<CartItem> cartItemList = cartItemServiceObj.findByShoppingCart(user.getShoppingCart());


            model.addAttribute("shippingAddress", shippingAddressObj);
            model.addAttribute("payment", paymentObj);
            model.addAttribute("billingAddress", billingAddressObj);
            model.addAttribute("cartItemList", cartItemList);
            model.addAttribute("shoppingCart", user.getShoppingCart());

            List<String> stateList = USConstants.listOfUSStatesCode;
            Collections.sort(stateList);
            model.addAttribute("stateList", stateList);

            List<UserShipping> userShippingList = user.getUserShippingList();
            List<UserPayment> userPaymentList = user.getUserPaymentList();

            model.addAttribute("userShippingList", userShippingList);
            model.addAttribute("userPaymentList", userPaymentList);

            //se envia el true o false dependiend de la situacion.
            model.addAttribute("emptyPaymentList", userPaymentList.size() == 0);
            model.addAttribute("emptyShippingList", userShippingList.size() == 0);

            model.addAttribute("classActiveShipping", true);
            return "checkout";
        }
    }





    @GetMapping("/setPaymentMethod")
    public String setPaymentMethod(@RequestParam("userPaymentId") Long userPaymentId,
                                   Principal principal, Model model) {

        System.out.println("el id del userpayment es: " + userPaymentId);

        User user = userServiceObj.findByUsername(principal.getName());
        UserPayment userPayment = userPaymentServiceObj.findById(userPaymentId);
        UserBilling userBilling = userPayment.getUserBilling();

        if (!userPayment.getUser().getId().equals(user.getId())) {
            return "badRequestPage";
        } else {
            paymentServiceObj.setByUserPayment(userPayment, paymentObj);

            List<CartItem> cartItemList = cartItemServiceObj.findByShoppingCart(user.getShoppingCart());
            //billingAddressServiceObj.setByUserBilling(userBilling,billingAddressObj);

            System.out.println("el nuevo payment seleccionado tiene id: " + paymentObj.getId() + " con cardname: " + paymentObj.getHolderName());

            model.addAttribute("shippingAddress", shippingAddressObj);
            model.addAttribute("payment", paymentObj);
            model.addAttribute("billingAddress", billingAddressObj);
            model.addAttribute("cartItemList", cartItemList);
            model.addAttribute("shoppingCart", user.getShoppingCart());

            List<String> stateList = USConstants.listOfUSStatesCode;
            Collections.sort(stateList);
            model.addAttribute("stateList", stateList);

            List<UserShipping> userShippingList = user.getUserShippingList();
            List<UserPayment> userPaymentList = user.getUserPaymentList();

            //se envia el true o false dependiend de la situacion.
            model.addAttribute("emptyPaymentList", userPaymentList.size() == 0);
            model.addAttribute("emptyShippingList", userShippingList.size() == 0);

            model.addAttribute("userShippingList", userShippingList);
            model.addAttribute("userPaymentList", userPaymentList);

            model.addAttribute("classActivePayment", true);
            return "checkout";
        }
    }



    @PostMapping("/checkout")
    public String checkoutPost(@ModelAttribute("shippingAddress") ShippingAddress shippingAddress,
                               @ModelAttribute("billingAddress") BillingAddress billingAddress,
                               @ModelAttribute("payment") Payment payment,
                               @ModelAttribute("billingSameAsShipping") String billingSameAsShipping,
                               @ModelAttribute("shippingMethod") String shippingMethod, Principal principal, Model model) {

        ShoppingCart shoppingCart = userServiceObj.findByUsername(principal.getName()).getShoppingCart();
        List<CartItem> cartItemList = cartItemServiceObj.findByShoppingCart(shoppingCart);

        model.addAttribute("cartItemList", cartItemList);

        if (billingSameAsShipping.equals("true")) {
            billingAddress.setBillingAddressName(shippingAddress.getShippingAddressName());
            billingAddress.setBillingAddressStreet1(shippingAddress.getShippingAddressStreet1());
            billingAddress.setBillingAddressStreet2(shippingAddress.getShippingAddressStreet2());
            billingAddress.setBillingAddressCity(shippingAddress.getShippingAddressCity());
            billingAddress.setBillingAddressState(shippingAddress.getShippingAddressState());
            billingAddress.setBillingAddressCountry(shippingAddress.getShippingAddressCountry());
            billingAddress.setBillingAddressZipcode(shippingAddress.getShippingAddressZipcode());
        }

        if (shippingAddress.getShippingAddressStreet1().isEmpty() || shippingAddress.getShippingAddressCity().isEmpty()
                || shippingAddress.getShippingAddressState().isEmpty()
                || shippingAddress.getShippingAddressName().isEmpty()
                || shippingAddress.getShippingAddressZipcode().isEmpty() || payment.getCardNumber().isEmpty()
                || payment.getCvc() == 0 || billingAddress.getBillingAddressStreet1().isEmpty()
                || billingAddress.getBillingAddressCity().isEmpty() || billingAddress.getBillingAddressState().isEmpty()
                || billingAddress.getBillingAddressName().isEmpty()
                || billingAddress.getBillingAddressZipcode().isEmpty())
            return "redirect:/checkout?id=" + shoppingCart.getId() + "&missingRequiredField=true";

        User user = userServiceObj.findByUsername(principal.getName());

        //Organizacion de la fecha de envio
        //*********************************************************
        LocalDate today = LocalDate.now();
        LocalDate estimatedDeliveryDate;


        if (shippingMethod.equals("groundShipping")) {
            estimatedDeliveryDate = today.plusDays(5);
        } else {
            estimatedDeliveryDate = today.plusDays(3);
        }

        Date shippingDate = Date.from(estimatedDeliveryDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        //*********************************************************

        Order order = orderServiceObj.createOrder(shoppingCart, shippingAddress, billingAddress, payment, shippingMethod, user, shippingDate);

        mailSender.send(mailConstructor.constructOrderConfirmationEmail(user, order, Locale.ENGLISH));

        shoppingCartServiceObj.clearShoppingCart(shoppingCart);



        model.addAttribute("estimatedDeliveryDate", estimatedDeliveryDate);

        return "orderSubmittedPage";
    }


}
