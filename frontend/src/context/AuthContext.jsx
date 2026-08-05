import React,{createContext,useContext,useState} from 'react';
import {api} from '../services/api';
const C=createContext(null);
export function AuthProvider({children}){
 const [user,setUser]=useState(()=>{try{return JSON.parse(localStorage.getItem('user'))}catch{return null}});
 const login=async(username,password)=>{const r=await api('/auth/login',{method:'POST',body:JSON.stringify({username,password})});localStorage.setItem('token',r.token);localStorage.setItem('user',JSON.stringify(r));setUser(r);};
 const logout=()=>{localStorage.removeItem('token');localStorage.removeItem('user');setUser(null)};
 return <C.Provider value={{user,login,logout,authenticated:!!localStorage.getItem('token')}}>{children}</C.Provider>;
}
export const useAuth=()=>useContext(C);
