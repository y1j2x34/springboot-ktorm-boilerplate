import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "shadcn-solid-components/components/card";
import { SettingsLayout, SettingsSection } from "shadcn-solid-components/hoc/settings-layout";
import {
    RadioGroup,
    RadioGroupItem,
    RadioGroupItemControl,
    RadioGroupItemInput,
    RadioGroupItemLabel,
    RadioGroupItems,
    RadioGroupLabel,
  } from 'shadcn-solid-components/components/radio-group'
import { IconAppearance } from "~/icons/appearance";
import { useColorMode } from "@kobalte/core";

const sections: SettingsSection[] = [{
    id: 'appearance',
    label: '外观',
    icon: <IconAppearance/>
}];

function AppearanceSection() {
    const { colorMode, setColorMode } = useColorMode();
    return <div class="space-y-6">
        <Card>
        <CardHeader>
          <CardTitle>Color Mode</CardTitle>
          <CardDescription>Choose your preferred color mode.</CardDescription>

        </CardHeader>
        <CardContent>
          <RadioGroup
            value={colorMode()}
            onChange={v => setColorMode(v as 'light' | 'dark' | 'system')}
          >
            <RadioGroupLabel class="sr-only">Color Mode</RadioGroupLabel>
            <RadioGroupItems>
              <RadioGroupItem value="light">
                <RadioGroupItemInput />
                <RadioGroupItemControl />
                <RadioGroupItemLabel>Light</RadioGroupItemLabel>
              </RadioGroupItem>
              <RadioGroupItem value="dark">
                <RadioGroupItemInput />
                <RadioGroupItemControl />
                <RadioGroupItemLabel>Dark</RadioGroupItemLabel>
              </RadioGroupItem>
              <RadioGroupItem value="system">
                <RadioGroupItemInput />
                <RadioGroupItemControl />
                <RadioGroupItemLabel>System</RadioGroupItemLabel>
              </RadioGroupItem>
            </RadioGroupItems>
          </RadioGroup>
        </CardContent>
      </Card>
    </div>
}

export default function settings() {
    return <SettingsLayout
        sections={sections}
        defaultActiveSection=""
    >{{
        appearance: AppearanceSection
    }}</SettingsLayout>
}