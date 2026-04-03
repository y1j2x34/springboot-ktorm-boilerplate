import { Router, useLocation, useNavigate } from "@solidjs/router";
import { FileRoutes } from "@solidjs/start/router";
import { JSXElement, Suspense } from "solid-js";
import "./app.css";
import { AppSidebar, AppSidebarMenuGroup } from 'shadcn-solid-components/hoc/app-sidebar'
import { HeaderIcon } from "./components/header-icon";
import { IconAlertTriangle, IconArrowRight, IconBell, IconCreditCard, IconFile, IconFullscreen, IconHash, IconHome, IconInbox, IconRocket, IconSettings, IconStar } from "shadcn-solid-components/icons";

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
    {
      group: 'Base Components',
      items: [
        {
          icon: () => <IconRocket class="size-4" />,
          title: 'General',
          get isActive() {
            return isActive('/general')
          },
          onClick: () => navigate('/general'),
        },
        {
          icon: () => <IconFullscreen class="size-4" />,
          title: 'Layout',
          get isActive() {
            return isActive('/layout')
          },
          onClick: () => navigate('/layout'),
        },
        {
          icon: () => <IconArrowRight class="size-4" />,
          title: 'Navigation',
          get isActive() {
            return isActive('/navigation')
          },
          onClick: () => navigate('/navigation'),
        },
        {
          icon: () => <IconFile class="size-4" />,
          title: 'Form Inputs',
          get isActive() {
            return isActive('/form-inputs')
          },
          onClick: () => navigate('/form-inputs'),
        },
        {
          icon: () => <IconInbox class="size-4" />,
          title: 'Data Display',
          get isActive() {
            return isActive('/data-display')
          },
          onClick: () => navigate('/data-display'),
        },
        {
          icon: () => <IconAlertTriangle class="size-4" />,
          title: 'Overlay & Feedback',
          get isActive() {
            return isActive('/overlay')
          },
          onClick: () => navigate('/overlay'),
        },
      ],
    },
    {
      group: 'Composites',
      items: [
        {
          icon: () => <IconHome class="size-4" />,
          title: 'Dashboard',
          get isActive() {
            return isActive('/') || isActive('/dashboard')
          },
          onClick: () => navigate('/'),
        },
        {
          icon: () => <IconHash class="size-4" />,
          title: 'Tables',
          get isActive() {
            return isActive('/tables')
          },
          onClick: () => navigate('/tables'),
        },
        {
          icon: () => <IconCreditCard class="size-4" />,
          title: 'Form Composites',
          get isActive() {
            return isActive('/forms-composite')
          },
          onClick: () => navigate('/forms-composite'),
        },
        {
          icon: () => <IconBell class="size-4" />,
          title: 'Feedback',
          get isActive() {
            return isActive('/feedback')
          },
          onClick: () => navigate('/feedback'),
        },
        {
          icon: () => <IconStar class="size-4" />,
          title: 'Display Composites',
          get isActive() {
            return isActive('/display-composite')
          },
          onClick: () => navigate('/display-composite'),
        },
        {
          icon: () => <IconSettings class="size-4" />,
          title: 'Custom Theme',
          get isActive() {
            return isActive('/custom-theme')
          },
          onClick: () => navigate('/custom-theme'),
        },
      ],
    },
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