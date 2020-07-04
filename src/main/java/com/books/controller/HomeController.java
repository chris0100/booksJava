package com.books.controller;

import com.books.domain.*;
import com.books.domain.security.PasswordResetToken;
import com.books.domain.security.Role;
import com.books.domain.security.UserRole;
import com.books.service.*;
import com.books.utility.MailConstructor;
import com.books.utility.SecurityUtility;
import com.books.utility.USConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.websocket.server.PathParam;
import java.security.Principal;
import java.util.*;

@Controller
public class HomeController {

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private MailConstructor mailConstructor;

    @Autowired
    private UserService userServiceObj;

    @Autowired
    private UserSecurityService userSecurityServiceObj;

    @Autowired
    private BookService bookServiceObj;

    @Autowired
    private UserPaymentService userPaymentServiceObj;

    @Autowired
    private UserShippingService userShippingServiceObj;

    @Autowired
    private OrderService orderServiceObj;

    @Autowired
    private CartItemService cartItemServiceObj;



    @GetMapping("/")
    public String index() {
        return "index";
    }



    @GetMapping("/login")
    public String login(Model model) {
        model.addAttribute("classActiveLogin", true);
        return "myAccount";
    }



    @GetMapping("/forgetPassword")
    public String forgetPassword(HttpServletRequest request, @ModelAttribute("email") String email,
                                 Model model) {

        model.addAttribute("classActiveForgetPassword", true);

        User user = userServiceObj.findByEmail(email);

        if (user == null) {
            model.addAttribute("emailNotExist", true);
            return "myAccount";
        }

        String password = SecurityUtility.randomPassword(); //genera una contraseña

        String encryptedPassword = SecurityUtility.passwordEncoder().encode(password); //encripta la contraseña
        user.setPassword(encryptedPassword); //asigna la contraseña encriptada al usuario

        userServiceObj.save(user); //guarda al usuario


        //realiza actividades del token para resetear el password enviando un correo electronico
        String token = UUID.randomUUID().toString();
        userServiceObj.createPasswordResetTokenForUser(user, token);

        String appUrl = "http://" + request.getServerName() + ":" + request.getServerPort() + request.getContextPath();

        SimpleMailMessage newEmail = mailConstructor.constructResetTokenEmail(appUrl, request.getLocale(), token, user, password);

        mailSender.send(newEmail);

        model.addAttribute("forgetPasswordEmailSent", "true");

        return "myAccount";
    }




    //se ingresan los datos de email y username para enviar datos de nuevo usuario, para luego ir al email y revisar datos
    @PostMapping("/newUser")
    public String newUserPost(HttpServletRequest request, @ModelAttribute("email") String userEmail,
                              @ModelAttribute("username") String username, Model model) throws Exception {

        model.addAttribute("classActiveNewAccount", true);
        model.addAttribute("email", userEmail);
        model.addAttribute("username", username);

        if (userServiceObj.findByUsername(username) != null) {
            model.addAttribute("usernameExists", true);

            return "myAccount";
        }

        if (userServiceObj.findByEmail(userEmail) != null) {
            model.addAttribute("emailExists", true);

            return "myAccount";
        }

        User user = new User();
        user.setUsername(username);
        user.setEmail(userEmail);

        String password = SecurityUtility.randomPassword();

        String encryptedPassword = SecurityUtility.passwordEncoder().encode(password);
        user.setPassword(encryptedPassword);

        Role role = new Role();
        role.setRoleId(1);
        role.setName("ROLE_USER");
        Set<UserRole> userRoles = new HashSet<>();
        userRoles.add(new UserRole(user, role));
        userServiceObj.createUser(user, userRoles);

        String token = UUID.randomUUID().toString();
        userServiceObj.createPasswordResetTokenForUser(user, token);

        String appUrl = "http://"+request.getServerName()+":"+request.getServerPort()+request.getContextPath();

        SimpleMailMessage email = mailConstructor.constructResetTokenEmail(appUrl, request.getLocale(), token, user, password);

        mailSender.send(email);

        model.addAttribute("emailSent", "true");
        //model.addAttribute("orderList", user.getOrderList());


        return "myAccount";
    }



    //esta se usa cuando es redirigido por el mensaje que se envia a la cuenta de correo.
    @GetMapping("/newUser")
    public String newUserToEdit(Locale locale, @RequestParam("token") String token, Model model) {

        PasswordResetToken passToken = userServiceObj.getPasswordResetToken(token);


        if (passToken == null) {
            String message = "Invalid Token.";
            model.addAttribute("message", message);
            return "redirect:/badRequest";
        }

        User user = passToken.getUser();
        String username = user.getUsername();

        UserDetails userDetails = userSecurityServiceObj.loadUserByUsername(username);

        Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, userDetails.getPassword(),
                userDetails.getAuthorities());

        SecurityContextHolder.getContext().setAuthentication(authentication);

        model.addAttribute("user", user);

        model.addAttribute("classActiveEdit", true);
        model.addAttribute("updateUserInfo",true);
        model.addAttribute("orderList", user.getOrderList());

        return "myProfile";
    }



    @GetMapping("/bookshelf")
    public String bookshelf(Model model, Principal principal) {
        if (principal != null) { //si hay un usuario logueado, toma sus datos
            String username = principal.getName();
            User user = userServiceObj.findByUsername(username);
            model.addAttribute("user", user);
        }

        List<Book> bookList = bookServiceObj.findAll();
        boolean emptyList = bookList.isEmpty();
        model.addAttribute("emptyList", emptyList);
        model.addAttribute("bookList", bookList);
        model.addAttribute("activeAll",true);
        return "bookshelf";
    }



    @GetMapping("/bookDetail")
    public String bookDetail(@PathParam("id") Long id, Model model, Principal principal) {
        if (principal != null) {
            String username = principal.getName();
            User user = userServiceObj.findByUsername(username);
            model.addAttribute("user", user);
        }

        Book book = bookServiceObj.findById(id);
        model.addAttribute("book", book);

        //conocer la cantidad de la lista
        List<Integer> qtyList = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
        model.addAttribute("qtyList", qtyList);
        model.addAttribute("qty", 1);
        return "bookDetail";

    }



    @GetMapping("/myProfile")
    public String myProfile(Principal principal, Model model) {
        String username = principal.getName();
        User user = userServiceObj.findByUsername(username);
        model.addAttribute("user", user);
        model.addAttribute("userPaymentList", user.getUserPaymentList());
        model.addAttribute("userShippingList", user.getUserShippingList());
        model.addAttribute("orderList",user.getOrderList());

        UserShipping userShipping = new UserShipping();
        model.addAttribute("userShipping", userShipping);

        model.addAttribute("listOfCreditCards", true);
        model.addAttribute("listOfShippingAddresses", true);

        List<String> stateList = USConstants.listOfUSStatesCode;
        Collections.sort(stateList);
        model.addAttribute("stateList", stateList);
        model.addAttribute("classActiveEdit", true);
        return "myProfile";
    }





    @GetMapping("/addNewCreditCard")
    public String addNewCreditCard(Model model, Principal principal){
        User user = userServiceObj.findByUsername(principal.getName());
        model.addAttribute("user",user);
        model.addAttribute("userPaymentList", user.getUserPaymentList());
        model.addAttribute("userShippingList", user.getUserShippingList());
        model.addAttribute("orderList", user.getOrderList());

        model.addAttribute("addNewCreditCard",true);
        model.addAttribute("classActiveBilling",true);
        model.addAttribute("listOfShippingAddresses", true);

        UserBilling userBilling = new UserBilling();
        UserPayment userPayment = new UserPayment();

        model.addAttribute("userBilling", userBilling);
        model.addAttribute("userPayment", userPayment);

        List<String> stateList = USConstants.listOfUSStatesCode;
        Collections.sort(stateList);
        model.addAttribute("stateList", stateList);

        return "myProfile";
    }




    @GetMapping("/listOfCreditCards")
    public String listOfCreditCards(Model model, Principal principal, HttpServletRequest request) {
        User user = userServiceObj.findByUsername(principal.getName());
        model.addAttribute("user", user);
        model.addAttribute("userPaymentList", user.getUserPaymentList());
        model.addAttribute("userShippingList", user.getUserShippingList());
        model.addAttribute("orderList", user.getOrderList());

        model.addAttribute("listOfCreditCards", true);
        model.addAttribute("classActiveBilling",true);
        model.addAttribute("listOfShippingAddresses", true);

        return "myProfile";
    }



    @PostMapping("/addNewCreditCard")
    public String addNewCreditCardPOST(@ModelAttribute("userPayment") UserPayment userPayment,
                                       @ModelAttribute("userBilling") UserBilling userBilling, Principal principal,
                                       Model model){
        User user = userServiceObj.findByUsername(principal.getName());
        userServiceObj.updateUserBilling(userBilling, userPayment, user);

        model.addAttribute("user", user);
        model.addAttribute("userPaymentList", user.getUserPaymentList());
        model.addAttribute( "userShippingList", user.getUserShippingList());
        model.addAttribute("listOfCreditCards",true );
        model.addAttribute("classActiveBilling",true );
        model.addAttribute("listOfShippingAddresses", true);
        model.addAttribute("updateUserPaymentInfo",true);
        model.addAttribute("orderList", user.getOrderList());


        return "myProfile";
    }



    @GetMapping("/updateCreditCard")
    public String updateCreditCard(@ModelAttribute("id") Long creditCardId, Principal principal, Model model){
        User user = userServiceObj.findByUsername(principal.getName());
        UserPayment userPayment = userPaymentServiceObj.findById(creditCardId);

        //compara los ids de los users
        if(!user.getId().equals(userPayment.getUser().getId())){
            System.out.println("cayo en un bad request");
            return "badRequestPage";
        } else{
            model.addAttribute("user", user);
            UserBilling userBilling = userPayment.getUserBilling();
            model.addAttribute("userPayment", userPayment);
            model.addAttribute("userBilling", userBilling);

            List<String> stateList = USConstants.listOfUSStatesCode;
            Collections.sort(stateList);
            model.addAttribute("stateList",stateList);

            model.addAttribute("userPaymentList", user.getUserPaymentList());
            model.addAttribute("userShippingList", user.getUserShippingList());

            model.addAttribute("addNewCreditCard",true);
            model.addAttribute("classActiveBilling",true);
            model.addAttribute("listOfShippingAddresses", true);
            model.addAttribute("orderList", user.getOrderList());


            return "myProfile";
        }
    }


    @GetMapping("/removeCreditCard")
    public String removeCreditCard(@ModelAttribute("id") Long creditCardId, Principal principal, Model model){
        User user = userServiceObj.findByUsername(principal.getName());
        UserPayment userPayment = userPaymentServiceObj.findById(creditCardId);

        //compara los ids de los users
        if(!user.getId().equals(userPayment.getUser().getId())){
            System.out.println("cayo en un bad request");
            return "badRequestPage";
        }
         else {
            model.addAttribute("user", user);
            userPaymentServiceObj.removeById(creditCardId);


            model.addAttribute("userPaymentList", user.getUserPaymentList());
            model.addAttribute("userShippingList", user.getUserShippingList());

            model.addAttribute("listOfCreditCard", true);
            model.addAttribute("classActiveBilling", true);
            model.addAttribute("listOfShippingAddresses", true);
        }

            return "myProfile";
    }




    @PostMapping("/setDefaultPayment")
    public String setDefaultPayment(@ModelAttribute("defaultUserPaymentId") Long defaultPaymentId,
                                    Principal principal, Model model){
        User user = userServiceObj.findByUsername(principal.getName());
        userServiceObj.setUserDefaultPayment(defaultPaymentId, user);

        model.addAttribute("userPaymentList", user.getUserPaymentList());
        model.addAttribute("userShippingList", user.getUserShippingList());

        model.addAttribute("user",user);
        model.addAttribute("listOfCreditCard", true);
        model.addAttribute("classActiveBilling", true);
        model.addAttribute("listOfShippingAddresses", true);

        return "myProfile";
    }




    @GetMapping("/listOfShippingAddresses")
    public String listOfShippingAddresses (Model model, Principal principal, HttpServletRequest request) {
        User user = userServiceObj.findByUsername(principal.getName());
        model.addAttribute("user", user);
        model.addAttribute("userPaymentList", user.getUserPaymentList());
        model.addAttribute("userShippingList", user.getUserShippingList());
        model.addAttribute("orderList", user.getOrderList());

        model.addAttribute("classActiveShipping",true);
        model.addAttribute("listOfShippingAddresses", true);

        return "myProfile";
    }




    @GetMapping("/addNewShippingAddress")
    public String addNewShippingAddress(Model model, Principal principal){
        User user = userServiceObj.findByUsername(principal.getName());
        model.addAttribute("user",user);
        model.addAttribute("userPaymentList", user.getUserPaymentList());
        model.addAttribute("userShippingList", user.getUserShippingList());
        model.addAttribute("orderList", user.getOrderList());

        model.addAttribute("addNewShippingAddress",true);
        model.addAttribute("classActiveShipping",true);


        UserShipping userShipping = new UserShipping();

        model.addAttribute("userShipping", userShipping);

        List<String> stateList = USConstants.listOfUSStatesCode;
        Collections.sort(stateList);
        model.addAttribute("stateList", stateList);

        return "myProfile";
    }



    @PostMapping("/addNewShippingAddress")
    public String addNewShippingAddressPOST(@ModelAttribute("userShipping") UserShipping userShipping,
                                            Principal principal, Model model){
        User user = userServiceObj.findByUsername(principal.getName());
        userServiceObj.updateUserShipping(userShipping, user);

        model.addAttribute("user",user);
        model.addAttribute("userShippingList", user.getUserShippingList());
        model.addAttribute("classActiveShipping",true);
        model.addAttribute("listOfShippingAddresses",true);
        model.addAttribute("orderList", user.getOrderList());


        return "myProfile";

    }




    @GetMapping("/updateUserShipping")
    public String updateUserShipping(@ModelAttribute("id") Long shippingAddressId, Principal principal, Model model){
        User user = userServiceObj.findByUsername(principal.getName());
        UserShipping userShipping = userShippingServiceObj.findById(shippingAddressId);

        //compara los ids de los users
        if(!user.getId().equals(userShipping.getUser().getId())){
            System.out.println("cayo en un bad request");
            return "badRequestPage";
        }
        else{
            model.addAttribute("user", user);
            model.addAttribute("userShipping", userShipping);

            List<String> stateList = USConstants.listOfUSStatesCode;
            Collections.sort(stateList);
            model.addAttribute("stateList",stateList);

            model.addAttribute("userPaymentList", user.getUserPaymentList());
            model.addAttribute("userShippingList", user.getUserShippingList());

            model.addAttribute("addNewShippingAddress",true);
            model.addAttribute("classActiveShipping",true);
            model.addAttribute("orderList", user.getOrderList());

            return "myProfile";
        }
    }





    @PostMapping("/setDefaultShippingAddress")
    public String setDefaultShippingAddress(@ModelAttribute("defaultShippingAddressId") Long defaultShippingAddressId,
                                    Principal principal, Model model){
        User user = userServiceObj.findByUsername(principal.getName());
        userServiceObj.setUserDefaultShipping(defaultShippingAddressId, user);

        model.addAttribute("userPaymentList", user.getUserPaymentList());
        model.addAttribute("userShippingList", user.getUserShippingList());

        model.addAttribute("user",user);
        model.addAttribute("classActiveShipping", true);
        model.addAttribute("listOfShippingAddresses", true);
        model.addAttribute("orderList", user.getOrderList());

        return "myProfile";
    }





    @GetMapping("/removeUserShipping")
    public String removeUserShipping(@ModelAttribute("id") Long userShippingId, Principal principal, Model model){
        User user = userServiceObj.findByUsername(principal.getName());
        UserShipping userShipping = userShippingServiceObj.findById(userShippingId);


        //compara los ids de los users
        if(!user.getId().equals(userShipping.getUser().getId())){
            System.out.println("cayo en un bad request");
            return "badRequestPage";
        }
        else{
            model.addAttribute("user", user);

            userShippingServiceObj.removeById(userShippingId);


            model.addAttribute("userPaymentList", user.getUserPaymentList());
            model.addAttribute("userShippingList", user.getUserShippingList());

            model.addAttribute("listOfShippingAddresses",true);
            model.addAttribute("classActiveShipping",true);
            model.addAttribute("orderList", user.getOrderList());

            return "myProfile";
        }
    }



    @PostMapping("/updateUserInfo")
    public String updateUserInfo(@ModelAttribute("user") User user, @ModelAttribute("newPassword") String newPassword, Model model) throws Exception{
        System.out.println("el id de user es: " + user.getId() + "y el username es " + user.getUsername());
        User currentUser = userServiceObj.findByUsername(user.getUsername());

        if (currentUser == null){
            throw new Exception("User not found");
        }


        //check email already exists
        if(userServiceObj.findByEmail(user.getEmail()) != null){
            if (!userServiceObj.findByEmail(user.getEmail()).getId().equals(currentUser.getId())){
                model.addAttribute("emailExists", true);
                return "myProfile";
            }

        }


        //check username already exists
        if (userServiceObj.findByUsername(user.getUsername()) != null) {
            if (!userServiceObj.findByUsername(user.getUsername()).getId().equals(currentUser.getId())) {
                model.addAttribute("usernameExists", true);
                return "myProfile";
            }
        }


        //update password
        System.out.println("user get password" + user.getPassword());
        if (newPassword != null && !newPassword.isEmpty()){
            BCryptPasswordEncoder passwordEncoder = SecurityUtility.passwordEncoder();
            String dbPassword = currentUser.getPassword();

            if (passwordEncoder.matches(user.getPassword(), dbPassword)){
                currentUser.setPassword(passwordEncoder.encode(newPassword));
            }
            else{
                model.addAttribute("incorrectPassword",true);
                return "myProfile";
            }
        }

        currentUser.setFirstName(user.getFirstName());
        currentUser.setLastName(user.getLastName());
        currentUser.setUsername(user.getUsername());
        currentUser.setEmail(user.getEmail());
        userServiceObj.save(currentUser);

        model.addAttribute("updateSuccess", true);
        model.addAttribute("user",currentUser);
        model.addAttribute("classActiveEdit",true);

        //hace procedimiento de login
        UserDetails userDetails = userSecurityServiceObj.loadUserByUsername(currentUser.getUsername());

        Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, userDetails.getPassword(),
                userDetails.getAuthorities());

        SecurityContextHolder.getContext().setAuthentication(authentication);

        model.addAttribute("listOfShippingAddresses", true);
        model.addAttribute("listOfCreditCards", true);

        return "myProfile";
    }



    @GetMapping("/orderDetail")
    public String orderDetail(@RequestParam("id") Long orderId, Principal principal, Model model){
        User user = userServiceObj.findByUsername(principal.getName());
        Order order = orderServiceObj.findOne(orderId);

        System.out.println("el order id " + order);
        System.out.println("el user id " + user.getId());


        if (order.getUser().getId() != user.getId()){
            return "badRequestPage";
        }
        else{

            List<CartItem> cartItemList = cartItemServiceObj.findByOrder(order);
            System.out.println("el cartItemList tiene tamaño "+cartItemList.size());

            model.addAttribute("cartItemList", cartItemList);
            model.addAttribute("user",user);
            model.addAttribute("order",order);
            model.addAttribute("orderList",user.getOrderList());

            UserShipping userShipping = new UserShipping();
            model.addAttribute("userShipping", userShipping);

            List<String> stateList = USConstants.listOfUSStatesCode;
            Collections.sort(stateList);
            model.addAttribute("stateList",stateList);

            model.addAttribute("userPaymentList", user.getUserPaymentList());
            model.addAttribute("userShippingList", user.getUserShippingList());

            model.addAttribute("addNewShippingAddress",true);
            model.addAttribute("classActiveOrders",true);
            model.addAttribute("displayOrderDetail", true);

            return "myProfile";
        }
    }

    @GetMapping("/hours")
    public String hours(){
        return "hours";
    }

    @GetMapping("/faq")
    public String faq(){
        return "faq";
    }



















}
