$(document).ready(function () {
    //cuando se realice un cambio, esta atento, para mostrar un boton
    $(".cartItemQty").on('change', function () {
        var id = this.id;
        console.log('se han hecho cambios con id: ' + id);
        $('#update-item-' + id).css('display', 'inline-block')
    });

    //al dar click en la casillas se llama a la funcion
    $("#theSameAsShippingAddress").on('click', checkBillingAddress);

    //revisa cuando se ingresan las contraseñas, para que coincidan
    $("#txtConfirmPassword").keyup(checkPasswordMatch);
    $("#txtNewPassword").keyup(checkPasswordMatch);
})


//deshabilita los input que tienen la clase billing address
function checkBillingAddress() {
    if ($("#theSameAsShippingAddress").is(":checked")){
        $('.billingAddress').prop("disabled", true);
    }
    else{
        $('.billingAddress').prop("disabled", false);
    }
}



//comparar password en usuario registrado
function checkPasswordMatch() {
    var password = $("#txtNewPassword").val();
    var confirmPassword = $("#txtConfirmPassword").val();
    console.log('este es el pasword nuevo ' + password);
    console.log('este es la confirmacion del password ' + confirmPassword);


    if (password == "" && confirmPassword == ""){
        $("#checkPasswordMatch").html("");
        $("#updateUserInfoButton").prop('disabled',false);
    }
    else{
        if (password != confirmPassword){
            $("#checkPasswordMatch").html("Passwords do not match!");
            $("#updateUserInfoButton").prop('disabled',true);
        }
        else{
            $("#checkPasswordMatch").html("Passwords match!");
            $("#updateUserInfoButton").prop('disabled',false);
        }

    }
}

