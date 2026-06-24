import { Routes, Route, Navigate } from 'react-router-dom'
import { useAuth } from './context/AuthContext'
import LoginPage from './components/Auth/LoginPage'
import RegisterPage from './components/Auth/RegisterPage'
import AppLayout from './components/AppLayout/AppLayout'
import DashboardPage from './components/Dashboard/DashboardPage'
import TaskListPage from './components/TaskList/TaskListPage'
import CalendarPage from './components/Calendar/CalendarPage'
import CoursePage from './components/Course/CoursePage'
import WellnessPage from './components/Wellness/WellnessPage'
import AICoachPage from './components/AICoach/AICoachPage'
import StatsPage from './components/Stats/StatsPage'
import ProfilePage from './components/Profile/ProfilePage'
import MetaPage from './components/Meta/MetaPage'
import AttendancePage from './components/Attendance/AttendancePage'
import NotificationsPage from './components/Notifications/NotificationsPage'

function PrivateRoute({ children }: { children: JSX.Element }) {
  const { token } = useAuth()
  return token ? children : <Navigate to="/login" replace />
}

export default function App() {
  return (
    <Routes>
      <Route path="/login" element={<LoginPage />} />
      <Route path="/register" element={<RegisterPage />} />
      <Route path="/" element={<PrivateRoute><AppLayout /></PrivateRoute>}>
        <Route index element={<Navigate to="/dashboard" replace />} />
        <Route path="dashboard" element={<DashboardPage />} />
        <Route path="tareas" element={<TaskListPage />} />
        <Route path="calendario" element={<CalendarPage />} />
        <Route path="cursos" element={<CoursePage />} />
        <Route path="bienestar" element={<WellnessPage />} />
        <Route path="coach" element={<AICoachPage />} />
        <Route path="estadisticas" element={<StatsPage />} />
        <Route path="metas" element={<MetaPage />} />
        <Route path="asistencia" element={<AttendancePage />} />
        <Route path="notificaciones" element={<NotificationsPage />} />
        <Route path="perfil" element={<ProfilePage />} />
      </Route>
      <Route path="*" element={<Navigate to="/dashboard" replace />} />
    </Routes>
  )
}
