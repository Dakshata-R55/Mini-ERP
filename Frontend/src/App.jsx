import { useState } from 'react'
import LoginPage from './Pages/LoginPage'
import SignupPage from './Pages/SignupPage'
import ComingSoonPage from './Pages/ComingSoonPage'

export default function App() {
  const [screen, setScreen] = useState('login')
  const [loginMode, setLoginMode] = useState('user')
  const [session, setSession] = useState(null)

  function handleSuccess(data) {
    setSession(data)
    setScreen('home')
  }

  function handleSignOut() {
    setSession(null)
    setScreen('login')
    setLoginMode('user')
  }

  if (screen === 'signup') {
    return (
      <SignupPage
        onGoLogin={() => setScreen('login')}
        onSuccess={handleSuccess}
      />
    )
  }

  if (screen === 'home' && session) {
    return <ComingSoonPage session={session} onSignOut={handleSignOut} />
  }

  return (
    <LoginPage
      mode={loginMode}
      onSwitchMode={() => setLoginMode((m) => (m === 'user' ? 'admin' : 'user'))}
      onGoSignup={() => setScreen('signup')}
      onSuccess={handleSuccess}
    />
  )
}