import React, { useState } from 'react';
import { api } from '../services/api';
import { ErrorBox, PageHeader } from '../components/UI';

export default function Assistant() {
  const [phone, setPhone] = useState('51938149352');
  const [name, setName] = useState('Cliente de prueba');
  const [message, setMessage] = useState('menu');
  const [chat, setChat] = useState([]);
  const [error, setError] = useState('');

  async function send(event) {
    event.preventDefault();
    if (!message.trim()) return;
    const mine = message;
    setChat(current => [...current, { from: 'user', text: mine }]);
    setMessage('');
    setError('');
    try {
      const response = await api('/chat/message', {
        method: 'POST',
        body: JSON.stringify({ phone, customerName: name, message: mine })
      });
      setChat(current => [...current, {
        from: 'bot',
        text: response.reply,
        state: response.state,
        mediaUrl: response.mediaUrl,
        mediaType: response.mediaType
      }]);
    } catch (requestError) {
      setError(requestError.message);
    }
  }

  return <>
    <PageHeader
      title="Mashico — Asistente automático"
      subtitle="Prueba horarios, reservas, imágenes, pagos y pedidos reales"
    />
    <div className="assistant-layout">
      <div className="panel assistant-config">
        <label>Teléfono
          <input value={phone} onChange={event => setPhone(event.target.value)} />
        </label>
        <label>Nombre del cliente
          <input value={name} onChange={event => setName(event.target.value)} />
        </label>
        <p>
          Prueba con <b>menu</b>, <b>horario</b>, <b>reservar</b>, <b>bistec</b> o sigue el flujo numérico. Los pedidos confirmados descuentan stock y se registran en Supabase.
        </p>
        <button className="ghost" onClick={() => setChat([])}>Limpiar conversación</button>
      </div>
      <div className="phone">
        <div className="phone-head">🥩 Mashico · Carnicería La Herradura</div>
        <div className="messages">
          {chat.length === 0 && <div className="hint">Escribe “menu” para comenzar</div>}
          {chat.map((item, index) => <div key={index} className={'bubble ' + item.from}>
            {item.mediaUrl && <img className="chat-media" src={item.mediaUrl} alt="Contenido enviado por Mashico" />}
            {item.text.split('\n').map((line, lineIndex) => <React.Fragment key={lineIndex}>{line}<br /></React.Fragment>)}
            {item.state && <small>{item.state}</small>}
          </div>)}
        </div>
        <form className="chat-input" onSubmit={send}>
          <input value={message} onChange={event => setMessage(event.target.value)} placeholder="Escribe un mensaje..." />
          <button>➤</button>
        </form>
      </div>
    </div>
    <ErrorBox message={error} />
  </>;
}
