import React from 'react';
import {Routes,Route,Navigate} from 'react-router-dom';
import {useAuth} from './context/AuthContext';
import Layout from './components/Layout';
import Login from './pages/Login';import Dashboard from './pages/Dashboard';import Products from './pages/Products';import Orders from './pages/Orders';import Customers from './pages/Customers';import Delivery from './pages/Delivery';import Promotions from './pages/Promotions';import Assistant from './pages/Assistant';import Settings from './pages/Settings';import PublicCatalog from './pages/PublicCatalog';
function Private({children}){const {authenticated}=useAuth();return authenticated?children:<Navigate to="/login" replace/>}
export default function App(){return <Routes><Route path="/catalogo" element={<PublicCatalog/>}/><Route path="/login" element={<Login/>}/><Route path="/*" element={<Private><Layout><Routes><Route index element={<Navigate to="/dashboard" replace/>}/><Route path="dashboard" element={<Dashboard/>}/><Route path="pedidos" element={<Orders/>}/><Route path="productos" element={<Products/>}/><Route path="clientes" element={<Customers/>}/><Route path="delivery" element={<Delivery/>}/><Route path="promociones" element={<Promotions/>}/><Route path="asistente" element={<Assistant/>}/><Route path="configuracion" element={<Settings/>}/></Routes></Layout></Private>}/></Routes>}
