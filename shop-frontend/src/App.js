import React, { useState, useEffect } from 'react';

export default function App() {
    const API = 'http://localhost:8080/api/v1';
    const [products, setProducts] = useState([]);
    const [cart, setCart] = useState({ items: [] });

    // Ürünleri ve sepeti çek
    useEffect(() => {
        fetch(`${API}/products?page=0&size=10`)
            .then(res => {
                if (!res.ok) throw new Error(`Status: ${res.status}`);
                return res.json();
            })
            .then(data => setProducts(data.content || []))
            .catch(err => console.error('Ürün yükleme hatası:', err));

        fetch(`${API}/cart/1`)
            .then(res => {
                if (!res.ok) throw new Error(`Status: ${res.status}`);
                return res.json();
            })
            .then(data => setCart(data))
            .catch(err => console.error('Sepet yükleme hatası:', err));
    }, []);

    // Sepete ürün ekle
    const addToCart = (productId) => {
        fetch(`${API}/cart/1/items`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ productId, quantity: 1 })
        })
            .then(res => {
                if (!res.ok) throw new Error(`Status: ${res.status}`);
                return fetch(`${API}/cart/1`);
            })
            .then(res => res.json())
            .then(setCart)
            .catch(err => console.error('Sepete ekleme hatası:', err));
    };

    // Checkout işlemi
    const checkout = () => {
        fetch(`${API}/orders/from-cart/1`, { method: 'POST' })
            .then(res => {
                if (!res.ok) throw new Error(`Status: ${res.status}`);
                alert('Sipariş başarıyla oluşturuldu!');
                return fetch(`${API}/cart/1`);
            })
            .then(res => res.json())
            .then(setCart)
            .catch(err => console.error('Checkout hatası:', err));
    };

    return (
        <div style={{ display: 'flex', padding: 16, fontFamily: 'Arial, sans-serif' }}>
            {/* Ürün Listesi */}
            <div style={{ flex: 2, marginRight: 16 }}>
                <h1>Ürünler</h1>
                <div style={{ display: 'flex', flexWrap: 'wrap', gap: 16 }}>
                    {products.map(p => (
                        <div key={p.id} style={{ border: '1px solid #ccc', borderRadius: 8, padding: 12, width: 180 }}>
                            <h2 style={{ fontSize: 18, margin: '0 0 8px' }}>{p.name}</h2>
                            <p style={{ fontSize: 14, margin: '0 0 8px', minHeight: 40 }}>{p.description}</p>
                            <p style={{ fontWeight: 'bold', margin: '0 0 8px' }}>{p.price.toLocaleString()}₺</p>
                            <button
                                onClick={() => addToCart(p.id)}
                                style={{ padding: '6px 12px', background: '#007bff', color: '#fff', border: 'none', borderRadius: 4, cursor: 'pointer' }}
                            >
                                Sepete Ekle
                            </button>
                        </div>
                    ))}
                </div>
            </div>

            {/* Sepet Bölümü */}
            <div style={{ flex: 1 }}>
                <h1>Sepet</h1>
                {cart.items.length === 0 ? (
                    <p>Sepetiniz boş</p>
                ) : (
                    <ul style={{ listStyle: 'none', padding: 0 }}>
                        {cart.items.map(item => (
                            <li key={item.id} style={{ marginBottom: 8, padding: 8, background: '#f8f9fa', borderRadius: 4 }}>
                                Ürün #{item.productId} — Adet: {item.quantity}
                            </li>
                        ))}
                    </ul>
                )}
                <button
                    onClick={checkout}
                    style={{ marginTop: 16, padding: '8px 16px', background: '#28a745', color: '#fff', border: 'none', borderRadius: 4, cursor: 'pointer' }}
                >
                    Satın Al
                </button>
            </div>
        </div>
    );
}