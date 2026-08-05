import React from 'react';
export function PageHeader({title,subtitle,action}){return <div className="page-header"><div><h1>{title}</h1>{subtitle&&<p>{subtitle}</p>}</div>{action}</div>}
export function Empty({text='No hay registros'}){return <div className="empty">{text}</div>}
export function Loading(){return <div className="loading">Cargando...</div>}
export function ErrorBox({message}){return message?<div className="error-box">{message}</div>:null}
export function Status({value}){return <span className={'status '+String(value).toLowerCase()}>{String(value).replaceAll('_',' ')}</span>}
export function Modal({title,onClose,children}){return <div className="modal-backdrop"><div className="modal"><div className="modal-head"><h2>{title}</h2><button onClick={onClose}>✕</button></div>{children}</div></div>}
