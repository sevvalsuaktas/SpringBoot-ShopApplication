package com.example.shop.shop.model;

public enum OrderStatus {
    NEW,       // sepetteki öğelerle yeni oluşmuş
    PROCESSING,// ödemesi/işlemesi devam ediyor
    COMPLETED, // başarılı tamamlandı
    CANCELLED  // iptal edildi
}
