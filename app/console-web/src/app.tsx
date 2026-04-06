import { Router, useLocation, useNavigate } from "@solidjs/router";
import { FileRoutes } from "@solidjs/start/router";
import { JSXElement, Suspense } from "solid-js";
import "./app.css";
import { AppSidebar, AppSidebarMenuGroup } from 'shadcn-solid-components/hoc/app-sidebar'
import { HeaderIcon } from "./components/header-icon";
import { IconAlertTriangle, IconArrowRight, IconBell, IconCreditCard, IconFile, IconFullscreen, IconHash, IconHome, IconInbox, IconRocket, IconSettings, IconStar } from "shadcn-solid-components/components/icons";

fetch('/api/public/auth/me')
.then(resp => {
  if(resp.status === 401) {
    location.href = '/api/login'
  }
})
export default function App() {
  return (
    <Router
      root={props => (
        <AppInner {...props} />
      )}
    >
      <FileRoutes />
    </Router>
  );
}

function AppInner(props: { children?: JSXElement }) {

  const location = useLocation()
  const navigate = useNavigate()

  const pathname = () => location.pathname
  const isActive = (path: string) => {
    const p = pathname()
    if (path === '/') return p === '/' || p === ''
    return p === path || p === `${path}/`
  }
  const menus: AppSidebarMenuGroup[] = [
    
  ]
  return <>
  <AppSidebar
    header={
      {
        icon: <HeaderIcon />,
        title: 'Shadcn Solid',
      }
    }
    menus={menus}
    footer={<></>}
    body = {
      <Suspense>{props.children}</Suspense>
    } 
  >
  </AppSidebar>
</>
}